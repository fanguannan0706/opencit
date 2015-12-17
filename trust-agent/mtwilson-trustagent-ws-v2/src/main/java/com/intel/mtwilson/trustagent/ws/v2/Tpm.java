/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.ws.v2;

import com.intel.dcsg.cpg.net.IPv4Address;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.commands.BuildQuoteXMLCmd;
import com.intel.mountwilson.trustagent.commands.CreateNonceFileCmd;
import com.intel.mountwilson.trustagent.commands.GenerateModulesCmd;
import com.intel.mountwilson.trustagent.commands.GenerateQuoteCmd;
import com.intel.mountwilson.trustagent.commands.ReadIdentityCmd;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.trustagent.commands.ReadAssetTag;
import com.intel.mountwilson.trustagent.commands.RetrieveTcbMeasurement;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.trustagent.model.TpmQuoteRequest;
import com.intel.mtwilson.trustagent.model.TpmQuoteResponse;
import com.intel.mtwilson.util.exec.EscapeUtil;
import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/tpm")
public class Tpm {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Tpm.class);

    /*
    @POST
    @Path("/quote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] tpmQuoteBytes(TpmQuoteRequest tpmQuoteRequest, @Context HttpServletRequest request) throws IOException, TAException {
        return null;
    }
    */
    
    @POST
    @Path("/quote")
    @Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public TpmQuoteResponse tpmQuote(TpmQuoteRequest tpmQuoteRequest, @Context HttpServletRequest request) throws IOException, TAException {
        /**
         * issue #1038 we will hash this ip address together with the input
         * nonce to produce the quote nonce; mtwilson server will do the same
         * thing; this prevents a MITM "quote relay" attack where an attacker
         * can accept quote requests at host A, forward them to trusted host B,
         * and then reply with host B's quote to the challenger (same nonce etc)
         * because with this fix mtwilson is hashing host A's ip address into
         * the nonce on its end, and host B is hashing its ip address into the
         * nonce (here), so the quote will fail the challenger's verification
         * because of the different nonces; Attacker will also not be able to
         * cheat by hashing host B's ip address into the nonce because host B
         * will again has its ip address so it will be double-hashed and fail
         * verification
         */
        TrustagentConfiguration configuration = TrustagentConfiguration.loadConfiguration();
        if( configuration.isTpmQuoteWithIpAddress() ) {
            if( IPv4Address.isValid(request.getLocalAddr()) ) {
                IPv4Address ipv4 = new IPv4Address(request.getLocalAddr());
                byte[] extendedNonce = Sha1Digest.digestOf(tpmQuoteRequest.getNonce()).extend(ipv4.toByteArray()).toByteArray(); // again 20 bytes
                tpmQuoteRequest.setNonce(extendedNonce);
            }
            else {
                log.debug("Local address is {}", request.getLocalAddr());
                throw new WebApplicationException(Response.serverError().header("Error", "tpm.quote.ipv4 enabled but local address not IPv4").build());
            }
        }
          
        TADataContext context = new TADataContext(); // when we call getSessionId it will create a new random one
        String osName = System.getProperty("os.name");
        context.setOsName(osName);

        /* If it is Windows host, Here we read Geotag from nvram index 0x40000010 and do sha1(nonce | geotag) and use the result as the nonce for TPM quote
           As of now, we still keep the same geotag provisioning mechanism by writing it to TPM. there are other approaches as well, but not in implementation.
        */  
        boolean isTagProvisioned = false;
        byte[] assetTagHash = null; 
        if (osName.toLowerCase().contains("windows")) {
            // 1. Trying to read AssetTag from TPM NVRAM 0x40000010
            new ReadAssetTag(context).execute();
            // 2. if provisioned, generate and set the new nonce
            String assetTag = context.getAssetTagHash();
            if (assetTag != null) {
                log.debug("Assetag is: {}", assetTag);
                try {
                    assetTagHash = Hex.decodeHex(assetTag.toCharArray());
                    if (assetTagHash.length == 20) { 
                        // sha1(nonce | assetTagHash)
                        log.debug("AssetTagHash is 20 bytes");
                        byte[] extendedNonceWithAssetTag = Sha1Digest.digestOf(tpmQuoteRequest.getNonce()).extend(assetTagHash).toByteArray();
                        tpmQuoteRequest.setNonce(extendedNonceWithAssetTag);
                        isTagProvisioned = true;
                    }
                    else {
                        log.debug("The asset tag read from NVRAM is not 20 bytes!");                  
                    }
                } catch (DecoderException ex) {
                    log.error("Decoding assettag hash error: {}", ex.getMessage());
                }
            }
            else {
                log.debug("AssetTag is not provisioned");
            }
        }

        context.setNonce(Base64.encodeBase64String(tpmQuoteRequest.getNonce()));

        context.setSelectedPCRs(joinIntegers(tpmQuoteRequest.getPcrs(), ' '));

        new CreateNonceFileCmd(context).execute(); // FileUtils.write to file nonce (binary)
        new ReadIdentityCmd(context).execute();  // trustagentrepository.getaikcertificate

        // Get the module information
        if (!osName.toLowerCase().contains("windows")) {
            new GenerateModulesCmd(context).execute(); // String moduleXml = getXmlFromMeasureLog(configuration);
            new RetrieveTcbMeasurement(context).execute(); //does nothing if measurement.xml does not exist
        }
        new GenerateQuoteCmd(context).execute();
        new BuildQuoteXMLCmd(context).execute();

//            return context.getResponseXML();
        TpmQuoteResponse response = context.getTpmQuoteResponse();
        // delete temporary session directory

        //assetTag 
        response.isTagProvisioned = isTagProvisioned;
        if (isTagProvisioned) {
            response.assetTag = assetTagHash;
        }

        // we should use more neutral ways to delete the folder
        FileUtils.forceDelete(new File(context.getDataFolder()));

        /*
        CommandUtil.runCommand(String.format("rm -rf %s",
                EscapeUtil.doubleQuoteEscapeShellArgument(context.getDataFolder())));


        CommandUtil.runCommand(String.format("rmdir /s /q %s",
                EscapeUtil.doubleQuoteEscapeShellArgument(context.getDataFolder())));
        */
        return response;
    }
    
    private String joinIntegers(int[] pcrs, char separator) {
        String[] array = new String[pcrs.length];
        for(int i=0; i<pcrs.length; i++) {
            array[i] = String.valueOf(pcrs[i]);
        }
        return StringUtils.join(array, separator);
    }
    
}
