/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jersey.Document;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="os")
public class Mle extends Document{
    
    public enum MleType {
        BIOS,
        VMM;
    }

    public enum AttestationType {
        PCR,
        MODULE;
    }
    
    private String name;
    private String version;
    private AttestationType attestationType;
    private MleType mleType;
    private String description;
    private String osUUID;
    private String oemUUID;
    private String source; // source host used for whitelisting

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public AttestationType getAttestationType() {
        return attestationType;
    }

    public void setAttestationType(AttestationType attestationType) {
        this.attestationType = attestationType;
    }

    public MleType getMleType() {
        return mleType;
    }

    public void setMleType(MleType mleType) {
        this.mleType = mleType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOsUUID() {
        return osUUID;
    }

    public void setOsUUID(String osUUID) {
        this.osUUID = osUUID;
    }

    public String getOemUUID() {
        return oemUUID;
    }

    public void setOemUUID(String oemUUID) {
        this.oemUUID = oemUUID;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
    
}
