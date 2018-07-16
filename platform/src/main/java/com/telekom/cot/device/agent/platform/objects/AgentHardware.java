package com.telekom.cot.device.agent.platform.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AgentHardware implements AgentFragment {
	private String model;
	private String revision;
	private String serialNumber;

	public AgentHardware(String model, String revision, String serialNumber) {
		this.model = model;
		this.revision = revision;
		this.serialNumber = serialNumber;
	}

	public String getModel() {
		return model;
	}

	public String getRevision() {
		return revision;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	@Override
	public String getId() {
		return "c8y_Hardware";
	}

	@Override
	public JsonElement getJson() {
		JsonObject object = new JsonObject();
		object.addProperty("model", model);
		object.addProperty("revision", revision);
		object.addProperty("serialNumber", serialNumber);

		return object;
	}
}
