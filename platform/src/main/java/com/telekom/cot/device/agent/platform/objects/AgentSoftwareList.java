package com.telekom.cot.device.agent.platform.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class AgentSoftwareList implements AgentFragment {
	private static final Gson gson = new Gson();

	private List<Software> softwareList = new ArrayList<>();

	public AgentSoftwareList(Software... softwareList) {
		Collections.addAll(this.softwareList, softwareList);
	}

	public List<Software> getSoftwareList() {
		// It's ok to give out our Software instances because they are immutable.
		return new ArrayList<>(softwareList);
	}

	public AgentSoftwareList addSoftware(Software software) {
		softwareList.add(software);
		return this;
	}

	public AgentSoftwareList removeSoftware(Software software) {
		softwareList.remove(software);
		return this;
	}

	@Override
	public String getId() {
		return "c8y_SoftwareList";
	}

	@Override
	public JsonElement getJson() {
		JsonArray array = new JsonArray();
		for (Software software : softwareList) {
			array.add(gson.toJsonTree(software));
		}

		return array;
	}

	public static class Software {
		public final String name;
		public final String version;
		public final String url;

		public Software(String name, String version, String url) {
			this.name = name;
			this.version = version;
			this.url = url;
		}
	}
}
