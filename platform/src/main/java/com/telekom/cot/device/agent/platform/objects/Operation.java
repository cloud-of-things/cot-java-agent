package com.telekom.cot.device.agent.platform.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Operation {
	private HashMap<String, Object> properties = new HashMap<>();

	public Operation() {
	}
	
    public Operation(String id) {
        this.setId(id);
    }
    
	@SuppressWarnings("unchecked")
    public Map<String, Object> getProperties() {
		return (Map<String, Object>) properties.clone();
	}
	
	public void setProperties(Map<String, Object> properties) {
		if(properties != null) {
			this.properties.putAll(properties);
		}
	}
	
	public void setProperty(String key, Object value) {
		this.properties.put(key, value);
	}
	
    public void removeProperty(String key) {
        this.properties.remove(key);
    }
    
	public Object getProperty(String key) {
		return properties.get(key);
	}
	
    public <T> T getProperty(String key, Class<T> type) {
        // check parameters
        if (Objects.isNull(key) || Objects.isNull(type)) {
            return null;
        }
        
        Object property = getProperty(key); 
        return type.isInstance(property) ? type.cast(property) : null;
    }
    
	public String getId() {
		return (String) getProperty("id");
	}
	
	public void setId(String id) {
		setProperty("id", id);
	}
	
	public OperationStatus getStatus() {
		return (OperationStatus) getProperty("status");
	}
	
	public void setStatus(OperationStatus status) {
		setProperty("status", status);
	}
	
	public String getDeviceId() {
		return (String) getProperty("deviceId");
	}
	
	public void setDeviceId(String deviceId) {
		setProperty("deviceId", deviceId);
	}
	
	public String getDeliveryType() {
		return (String) getProperty("deliveryType");
	}
	
	public void setDeliveryType(String deliveryType) {
		setProperty("deliveryType", deliveryType);
	}	

}
