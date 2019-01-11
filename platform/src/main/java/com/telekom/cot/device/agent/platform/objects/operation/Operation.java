package com.telekom.cot.device.agent.platform.objects.operation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * {@link Operation} represents an operation at the device agent 
 */
public abstract class Operation {
	private HashMap<String, Object> properties = new HashMap<>();

	/**
	 * default constructor
	 */
	public Operation() {
	    String operationName = getOperationName();
	    if (StringUtils.isNotEmpty(operationName)) {
	        setProperty(operationName, null);
	    }
	}
	
	/**
	 * constructor, set operation id
	 */
    public Operation(String id) {
        this();
        this.setId(id);
    }

    /**
     * constructor to set properties
     */
    public Operation(Map<String, Object> properties) {
        this();
        setProperties(properties);
    }
    
    /**
     * get the name of the operation by the {@link OperationAttributes} annotation,
     * e.g. "c8y_Restart" for the {@link RestartOperation} 
     * @return name of the operation or "" if there's no {@link OperationAttributes} annotation
     */
    public final String getOperationName() {
        OperationAttributes operationAttributes = this.getClass().getAnnotation(OperationAttributes.class);
        return Objects.nonNull(operationAttributes) ? operationAttributes.name() : "";
    }
    
    /**
     * get a duplicate of the properties map
     */
	@SuppressWarnings("unchecked")
    public Map<String, Object> getProperties() {
		return (Map<String, Object>) properties.clone();
	}

	/**
	 * set the given properties at this operation instance 
	 */
	public void setProperties(Map<String, Object> properties) {
		if(properties != null) {
			this.properties.putAll(properties);
		}
	}

	/**
	 * get the property value for the given property name
	 * @param propertyName name of the property to get
	 * @return the property value or {@code null} if property name is not valid or property not found
	 */
    public Object getProperty(String propertyName) {
        // check propertyName
        if (Objects.isNull(propertyName) || propertyName.isEmpty()) {
            return null;
        }
        
        return this.properties.get(propertyName);
    }
    
    /**
     * get the property value for the given property name as given type
     * @param propertyName name of the property to get
     * @param type type to get the property as
     * @return the property value or {@code null} if property name is not valid, property not found or
     * can't cast to given type
     */
    public <T> T getProperty(String propertyName, Class<T> type) {
        Object property = getProperty(propertyName); 

        // check property and type
        if (Objects.isNull(property) || Objects.isNull(type)) {
            return null;
        }
        
        return type.isInstance(property) ? type.cast(property) : null;
    }
    
    /**
     * set the property with given name to given value 
     */
	public void setProperty(String propertyName, Object value) {
		this.properties.put(propertyName, value);
	}
	
	/**
	 * removes the property with given name
	 */
    public void removeProperty(String propertyName) {
        this.properties.remove(propertyName);
    }

    /**
     * get the operation id
     */
	public String getId() {
		return getProperty("id", String.class);
	}
	
    /**
     * set the operation id
     */
	public void setId(String id) {
	    if(StringUtils.isNotEmpty(id)) {
	        setProperty("id", id);
	    }
	}
	
    /**
     * get the operation status
     */
	public OperationStatus getStatus() {
		return getProperty("status", OperationStatus.class);
	}
	
    /**
     * set the operation status
     */
	public void setStatus(OperationStatus status) {
		setProperty("status", status);
	}
	
    /**
     * get the device id
     */
	public String getDeviceId() {
		return getProperty("deviceId", String.class);
	}
	
    /**
     * set the device id
     */
	public void setDeviceId(String deviceId) {
		setProperty("deviceId", deviceId);
	}
	
    /**
     * get the delivery type
     */
	public String getDeliveryType() {
		return getProperty("deliveryType", String.class);
	}
	
    /**
     * set the delivery type
     */
	public void setDeliveryType(String deliveryType) {
		setProperty("deliveryType", deliveryType);
	}	
	
	/**
	 * get the fragment from property map by the (annotated) operation name
	 * <p/>e.g. for the {@link ConfigurationUpdateOperation} it looks for a property named "c8y_configuration"
	 * and returns the value as {@code Map<String, Object>}
	 * @return the fragment as map or {@code null} if fragment not found or an error occurs
	 */
	@SuppressWarnings("unchecked")
    protected Map<String, Object> getFragmentByOperationName() {
        try {
            return (Map<String, Object>)this.getProperty(this.getOperationName());
        } catch (Exception e) {
            return null;
        }
	}

	/**
	 * get a property value from the operation fragment in the property map
	 * <p/>
	 * e.g. the configuration update operation "c8y_configuration" contains a fragment
	 * ({@code Map <String, Object>}) named "c8y_configuration" and this fragment contains
	 * a property named "config": <br/>
	 * <pre>
	 * {
	 *   "id": "12345",
	 *   "c8y_configuration" : {
	 *     "config": "configuration string value"
	 *   },
	 *   "status": "PENDING"
	 * }
	 * </pre>
	 * To get the string "configuration string value", use
	 * <pre>
	 *    operation.getPropertyValueFromFragment("config", String.class, null);
	 * </pre>  
	 * @param fragmentPropertyName name of the property to get from the fragment
	 * @param defaultValue default value, returned if an error occurs or property value is {@code null}
	 * @param type type to cast to 
	 * @return property value if found or default value else
	 */
    protected <T> T getPropertyValueFromFragment(String fragmentPropertyName, T defaultValue, Class<T> type) {
        try {
            Object value = this.getFragmentByOperationName().get(fragmentPropertyName);
            return Objects.nonNull(value) && type.isInstance(value) ? type.cast(value) : defaultValue; 
        } catch (Exception e) {
            return defaultValue;
        }
	}

    /**
     * set a property value at the operation fragment in the property map
     * <p/>
     * e.g. the configuration update operation "c8y_configuration" contains a fragment
     * ({@code Map <String, Object>}) named "c8y_configuration" and this fragment contains
     * a property named "config": <br/>
     * <pre>
     * {
     *   "id": "12345",
     *   "c8y_configuration" : {
     *     "config": "configuration string value"
     *   },
     *   "status": "PENDING"
     * }
     * </pre>
     * To set the string "configuration string value", use
     * <pre>
     *    operation.setPropertyValueAtFragment("config", "configuration string value");
     * </pre>  
     * 
     * @param fragmentPropertyName name of the property to set at the fragment
     * @param value the value of the property to set
     */
    protected void setPropertyValueAtFragment(String fragmentPropertyName, Object value) {
        // get fragment map or create if not existant
        Map<String, Object> fragment = getFragmentByOperationName();
        if (Objects.isNull(fragment)) {
            fragment = new HashMap<>();
            setProperty(getOperationName(), fragment);
        }
        
        fragment.put(fragmentPropertyName, value);
    }
    
	/**
	 * an enumeration of possible operation states
	 */
	public enum OperationStatus {
	    /**
	     * Operation is retrieved by device, accepted and now executing. Next status can be SUCCESSFULL or FAILED.
	     */
	    EXECUTING,

	    /**
	     * Operation execution failed or device denied execution.
	     */
	    FAILED,

	    /**
	     * Operation is new and awaits retrieval from device.
	     */
	    PENDING,

	    /**
	     * Operation is executed successfully.
	     */
	    SUCCESSFUL,

	    /**
	     * Just used in device registry and indicates an accepted device.
	     */
	    ACCEPTED
	}
}
