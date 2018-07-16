package com.telekom.cot.device.agent.platform.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AgentFirmware implements AgentFragment {
	private String name;
	private String version;
	private String url;

	public AgentFirmware(String name, String version, String url) {
		this.name = name;
		this.version = version;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public String getId() {
		return "c8y_Firmware";
	}

	@Override
	public JsonElement getJson() {
		JsonObject object = new JsonObject();
		object.addProperty("name", name);
		object.addProperty("version", version);
		object.addProperty("url", url);

		return object;
	}
}
