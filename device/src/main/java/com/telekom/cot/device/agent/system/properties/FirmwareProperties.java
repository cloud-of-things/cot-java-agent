package com.telekom.cot.device.agent.system.properties;

public class FirmwareProperties extends Software implements Properties {

	public FirmwareProperties() {
	    super();
	}
	
    public FirmwareProperties(String name, String version, String url) {
        super(name, version, url);
    }
    
	@Override
	public String toString() {
		return FirmwareProperties.class.getSimpleName() + " [name=" + getName() + ", version=" + getVersion()
		            + ", url=" + getUrl() + "]";
	}
}
