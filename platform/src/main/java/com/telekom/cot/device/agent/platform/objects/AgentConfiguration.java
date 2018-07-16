package com.telekom.cot.device.agent.platform.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AgentConfiguration implements AgentFragment {
	private String config;

	public AgentConfiguration(String config) {
		this.config = config;
	}

	public String getConfig() {
		return config;
	}

	@Override
	public String getId() {
		return "c8y_Configuration";
	}

	@Override
	public JsonElement getJson() {
		JsonObject object = new JsonObject();
		object.addProperty("config", config);
		return object;
	}
}
