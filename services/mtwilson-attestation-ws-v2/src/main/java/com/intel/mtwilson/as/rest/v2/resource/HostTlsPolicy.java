/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicyCollection;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicyFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicyLocator;
import com.intel.mtwilson.as.rest.v2.repository.HostTlsPolicyRepository;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/hosts/{host}/tls-policy") // Should the user specified parameter here match the filterCriteria??
public class HostTlsPolicy extends AbstractJsonapiResource<com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy, HostTlsPolicyCollection, HostTlsPolicyFilterCriteria, NoLinks<com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy>, HostTlsPolicyLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private HostTlsPolicyRepository repository;
    
    public HostTlsPolicy() {
        repository = new HostTlsPolicyRepository();
    }
    
    @Override
    protected HostTlsPolicyRepository getRepository() { return repository; }

    
    @Override
    protected HostTlsPolicyCollection createEmptyCollection() {
        return new HostTlsPolicyCollection();
    }
      
}