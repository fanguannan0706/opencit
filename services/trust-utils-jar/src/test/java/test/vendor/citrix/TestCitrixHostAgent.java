/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.citrix;

import test.vendor.intel.*;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrEventLog;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.mtwilson.tls.TlsPolicy;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In order to run these tests, you need aikqverify set up on your machine.  The following
 * documentation is copied from TAHelper:
 * 
 * In order to use the TAHelper, you need to have attestation-service.properties on your machine.
 * 
 * Here are example properties that Jonathan has at C:/Intel/CloudSecurity/attestation-service.properties:
 * 
com.intel.mountwilson.as.home=C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome
com.intel.mountwilson.as.aikqverify.cmd=aikqverify.exe
com.intel.mountwilson.as.openssl.cmd=openssl.bat
 * 
 * The corresponding files must exist. From the above example:
 * 
 *    C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome
 *    C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome/data   (can be empty, TAHelper will save files there)
 *    C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome/bin
 *         contains:  aikqverify.exe, cygwin1.dll
 * 
 * 
 * @author jbuhacoff
 */
public class TestCitrixHostAgent {
    private transient Logger log = LoggerFactory.getLogger(getClass());
    private static String hostname = "10.1.70.126";
    private static String connection = "citrix:https://10.1.70.126:443;root;P@ssw0rd";
    private static HostAgent agent;
    
    @BeforeClass
    public static void createHostAgent() throws KeyManagementException, MalformedURLException, IOException {
        agent = getAgent();
    }
    
    public static HostAgent getAgent() throws KeyManagementException, MalformedURLException, IOException {
        HostAgentFactory hostAgentFactory = new HostAgentFactory();
        ByteArrayResource tlsKeystore = new ByteArrayResource();
        TlsPolicy tlsPolicy = hostAgentFactory.getTlsPolicy("TRUST_FIRST_CERTIFICATE", tlsKeystore);
        HostAgent hostAgent = hostAgentFactory.getHostAgent(new ConnectionString(connection), tlsPolicy);
        return hostAgent;
    }
    
    
    /**
     * 
     * 
     * @throws IOException 
     */
    @Test
    public void getAikFromCitrixXen() throws IOException {
        PublicKey aik = agent.getAik();
        log.debug("Public key: {}", X509Util.encodePemPublicKey(aik));
        assertNotNull(aik);
        
    }
    
    /**
     * 
     * 
     * @throws IOException 
     */
    @Test
    public void getPcrManifestFromCitrixXen() throws IOException {
        PcrManifest pcrManifest = agent.getPcrManifest();
        assertNotNull(pcrManifest);
        
        for(int i=0; i<24; i++) {
            Pcr pcr = pcrManifest.getPcr(i);
            log.debug("Pcr {} = {}", i, pcr.getValue().toString());
        }
    }
    
    /**
     * Example output:
     * 
BIOS Name: null
BIOS Version: S5500.86B.01.00.T060.070620121139
BIOS OEM: Intel Corp.
VMM Name: Xen
VMM Version: 4.1.0
OS Name: SUSE LINUX
OS Version: 11
AIK Certificate: null
     * 
     * @throws IOException 
     */
    @Test
    public void getHostInformationFromXen() throws IOException {
        TxtHostRecord hostDetails = agent.getHostDetails();
        log.debug("BIOS Name: {}", hostDetails.BIOS_Name);
        log.debug("BIOS Version: {}", hostDetails.BIOS_Version);
        log.debug("BIOS OEM: {}", hostDetails.BIOS_Oem);
        log.debug("VMM Name: {}", hostDetails.VMM_Name);
        log.debug("VMM Version: {}", hostDetails.VMM_Version);
        log.debug("OS Name: {}", hostDetails.VMM_OSName);
        log.debug("OS Version: {}", hostDetails.VMM_OSVersion);
        log.debug("AIK Certificate: {}", hostDetails.AIK_Certificate);
    }
}