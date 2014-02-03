/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey2;

import com.intel.dcsg.cpg.classpath.JarClassIterator;
import com.intel.dcsg.cpg.extensions.ExtensionUtil;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.dcsg.cpg.util.ArrayIterator;
import com.intel.mtwilson.My;
import com.intel.mtwilson.Version;
import com.intel.mtwilson.launcher.DirectoryLauncher;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author jbuhacoff
 */
public class Util {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Util.class);
    
    public static File[] findAllJars() {
        try {
            // find the plugin directory, try My.configuration() first then the maven target folder
            File javaPath = new File(My.configuration().getMtWilsonJava());
            if( !javaPath.exists() ) {
                // try target/.../WEB-INF/lib in case we are running on a developer laptop
                javaPath = new File("target"+File.separator+"mtwilson-launcher-jetty9-"+Version.VERSION+File.separator+"WEB-INF"+File.separator+"lib");
            }
            log.debug("Relative path for testing: {}", javaPath.getAbsolutePath());
            if(javaPath.exists()) {
                log.debug("Found relative WEB-INF/lib");
            }
            else {
    //            targetWebinfLib = new File("C:\\Users\\jbuhacof\\workspace\\dcg_security-mtwilson\\integration\\mtwilson-launcher-jersey2\\target\\mtwilson-launcher-jersey2-1.2.3-SNAPSHOT\\WEB-INF\\lib");
                // check mtwilson home ... or maybe reverse the order?
            }
            DirectoryLauncher.JarFilter jarfilter = new DirectoryLauncher.JarFilter();
            return javaPath.listFiles(jarfilter);
        }
        catch(IOException e) {
            throw new RuntimeException("Cannot load configuration", e);
        }
    }

    public static void scanJars(File[] jars, Registrar[] registrars) {
        Iterator<File> it = new ArrayIterator<File>(jars); // only scans directory for jar files; does NOT scan subdirectories
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if(cl==null) { cl = Util.class.getClassLoader(); }
        while (it.hasNext()) {
            File jar = it.next();
            // XXX  for now we'll only scan mtwilson jar files  (mtwilson-* ) or jar files that were intended for use with mtwilson (some-other-company-mtwilson)
            if( !jar.getName().contains("mtwilson") ) { continue; }
            try {
                for(Registrar registrar : registrars) {
                    ExtensionUtil.scan(registrar, new JarClassIterator(jar, cl));// we use our current classloader which means if any classes are already loaded we'll reuse them
                }
            }
            catch(Throwable e) { // catch ClassNotFoundException and NoClassDefFoundError 
                log.error("Cannot read jar file {} because {}", jar.getAbsolutePath(), e.getClass().getName());
                // log.error("Cannot read jar file {}", jar.getAbsolutePath());
            }
        }
    }
    
}