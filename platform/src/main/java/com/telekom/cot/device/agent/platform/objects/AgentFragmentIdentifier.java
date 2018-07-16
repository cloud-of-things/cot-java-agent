package com.telekom.cot.device.agent.platform.objects;

public enum AgentFragmentIdentifier {
	HARDWARE("c8y_Hardware"), CONFIGURATION("c8y_Configuration"), FIRMWARE("c8y_Firmware"), MOBILE(
			"c8y_Mobile"), SOFTWARE_LIST("c8y_SoftwareList");

	private String id;

	AgentFragmentIdentifier(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}