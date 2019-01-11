package com.telekom.cot.device.agent.system.properties;

import java.util.Objects;

public class Software {
    private String name;
    private String version;
    private String url;

    public Software() {
    }
    
    public Software(String name, String version, String url) {
        this.name = name;
        this.version = version;
        this.url = url;
    }
    
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return Software.class.getSimpleName() + " [name=" + name + ", version=" + version + ", url=" + url
                + "]";
    }

    public boolean equals(Software software, boolean compareUrl) {
        // check parameter 'software'
        if (Objects.isNull(software)) {
            return false;
        }

        // compare url only if requested (by parameter 'compareUrl')
        boolean urlEquals = compareUrl ? url.equals(software.getUrl()) : true;
        
        return name.equals(software.getName()) && version.equals(software.version) && urlEquals;
    }
}
