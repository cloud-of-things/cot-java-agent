package com.telekom.cot.device.agent.system.properties;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class SoftwareProperties implements Properties {

	private List<Software> softwareList = new ArrayList<>();

	public List<Software> getSoftwareList() {
		return softwareList;
	}

	public void addSoftware(String name, String version, String url) {
	    // check name and version
	    if (StringUtils.isEmpty(name) || StringUtils.isEmpty(version)) {
	        return;
	    }
	    
	    // check if name already exists
        if (softwareList.stream().filter(o -> o.getName().equals(name)).findFirst().isPresent()) {
            return;
        }
        
        softwareList.add(new Software(name, version, url));
	}
	
	@Override
	public String toString() {
		return SoftwareProperties.class.getSimpleName() + " [softwareList=" + softwareList + "]";
	}
}
