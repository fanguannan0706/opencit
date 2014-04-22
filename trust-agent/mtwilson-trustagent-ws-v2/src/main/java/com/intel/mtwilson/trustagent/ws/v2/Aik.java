/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.ws.v2;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FileUtils;

/**
 * This was previously called create_identity
 * 
 * @author jbuhacoff
 */
@V2
@Path("/aik")
public class Aik {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Aik.class);
    
    protected TrustagentConfiguration getConfiguration() throws IOException {
        return TrustagentConfiguration.loadConfiguration();
    }
    
    @GET
    @Produces({OtherMediaType.APPLICATION_PKIX_CERT, OtherMediaType.APPLICATION_X_PEM_FILE})
    public X509Certificate getIdentity(@Context HttpServletResponse response) throws IOException, CertificateException {
        TrustagentConfiguration configuration = getConfiguration();
        if( configuration.isDaaEnabled() ) {
            log.debug("daa is currently not supported");
//                new CreateIdentityDaaCmd(context).execute();
//                new BuildIdentityXMLCmd(context).execute();
            return null;
        }
        else {
            File aikCertificateFile = configuration.getAikCertificateFile();
            if( !aikCertificateFile.exists() ) {
                log.error("Missing AIK certificate file: {}", aikCertificateFile.getAbsolutePath());
//                response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
//                return null;
                throw new WebApplicationException(Response.serverError().header("Error", "Missing AIK certificate file").build());
            }
            String aikPem = FileUtils.readFileToString(aikCertificateFile);
            X509Certificate aikCertificate = X509Util.decodePemCertificate(aikPem);
            return aikCertificate;
        }
    }
 
    @GET
    @Path("/ca")
    @Produces({OtherMediaType.APPLICATION_PKIX_CERT, OtherMediaType.APPLICATION_X_PEM_FILE})
    public X509Certificate getIdentityCA(@Context HttpServletResponse response) throws Exception {
        TrustagentConfiguration configuration = getConfiguration();
        File keystoreFile = configuration.getTrustagentKeystoreFile();
        if( !keystoreFile.exists() ) {
            log.error("Missing keystore file: {}", keystoreFile.getAbsolutePath());
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            return null;
        }
        try {
            SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), configuration.getTrustagentKeystorePassword());
            X509Certificate privacyCACertificate = keystore.getX509Certificate("privacy", SimpleKeystore.CA);
            return privacyCACertificate;
        }
        catch(KeyManagementException | NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateEncodingException e) {
            log.error("Unable to load Privacy CA certificate from keystore file");
            log.debug("Unable to load Privacy CA certificate from keystore file", e);
//            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
//            return null;
            throw new WebApplicationException(Response.serverError().header("Error", "Cannot load Privacy CA certificate file").build());
        }
    }
}