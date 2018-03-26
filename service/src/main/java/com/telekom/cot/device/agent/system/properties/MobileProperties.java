package com.telekom.cot.device.agent.system.properties;

public class MobileProperties implements Properties {

	private String imei;
	private String cellId;
	private String iccid;

	public MobileProperties() {
	}
	
    public MobileProperties(String imei, String cellId, String iccid) {
        this.imei = imei;
        this.cellId = cellId;
        this.iccid = iccid;
    }
    
	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getCellId() {
		return cellId;
	}

	public void setCellId(String cellId) {
		this.cellId = cellId;
	}

	public String getIccid() {
		return iccid;
	}

	public void setIccid(String iccid) {
		this.iccid = iccid;
	}

	@Override
	public String toString() {
		return MobileProperties.class.getSimpleName() + " [imei=" + imei + ", cellId=" + cellId + ", iccid=" + iccid
				+ "]";
	}

}
