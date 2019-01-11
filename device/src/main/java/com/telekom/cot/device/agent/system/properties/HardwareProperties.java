package com.telekom.cot.device.agent.system.properties;

public class HardwareProperties implements Properties {

	private String model;
	private String revision;
	private String serialNumber;

	public HardwareProperties() {
	}
	
    public HardwareProperties(String model, String revision, String serialNumber) {
        this.model = model;
        this.revision = revision;
        this.serialNumber = serialNumber;
    }
    
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	@Override
	public String toString() {
		return HardwareProperties.class.getSimpleName() + " [model=" + model + ", revision=" + revision
				+ ", serialNumber=" + serialNumber + "]";
	}

}
