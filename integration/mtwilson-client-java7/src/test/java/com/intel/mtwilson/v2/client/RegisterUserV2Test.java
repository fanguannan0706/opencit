/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.v2.client;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.api.ClientException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class RegisterUserV2Test {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegisterUserV2Test.class);
   
    @Test
    public void testRegisterUserV2() throws MalformedURLException, IOException, ApiException, CryptographyException, ClientException {
        
        String userName = "TestAdmin999";
        String password = "password";
        URL server = new URL("https://10.1.71.234:8181/mtwilson/v2/");
        
        ByteArrayResource certResource = new ByteArrayResource();
        //SimpleKeystore keystore = KeystoreUtil.createUserInResourceV2(certResource, userName, password, server, "Admin role needed");
  
        // TODO: use MyConfiguration to select an appropriate path for local platform
        FileResource resource = new FileResource(new java.io.File("c:\\intel\\mtwilson\\"+userName+".jks"));
        SimpleKeystore keystore = MwClientUtil.createUserInResourceV2(resource, userName, password, server, "Admin role needed");
//        SimpleKeystore keystore = KeystoreUtil.createUserInDirectoryV2(new java.io.File("c:\\intel\\mtwilson"), userName, password, server, "Admin role needed");
        
    }
     
}