package com.telekom.cot.device.agent.platform.objects;

import java.util.HashMap;
import java.util.Map;

public class AgentManagedObject {
	protected final HashMap<String, Object> anyObject = new HashMap<>();

	public AgentManagedObject() {
	}

	public AgentManagedObject(Map<String, Object> attributes) {
		if (attributes != null) {
			anyObject.putAll(attributes);
		}
	}

	/**
	 * Set a custom attribute of the object. Setting the same property again will
	 * override the old value.
	 *
	 * @param attributeId
	 *            the unique id of the property as String
	 * @param value
	 *            the value of the property.
	 */
	public void set(String attributeId, Object value) {
		anyObject.put(attributeId, value);
	}

	/**
	 * Getting all attributes.
	 *
	 * @return a map with all attributes.
	 */
	@SuppressWarnings("unchecked")
    public Map<String, Object> getAttributes() {
		return (Map<String, Object>) anyObject.clone();
	}

	/**
	 * Adds all attributes from argument to this object. Attributes with the
	 * existing attribute identifiers will be overridden.
	 *
	 * @param attributes
	 *            the attributes to add, can't be null.
	 */
	public void setAttributes(Map<String, Object> attributes) {
		if (attributes != null) {
			anyObject.putAll(attributes);
		}
	}

	/**
	 * Get the unique identifier of the managed object. If the ManagedObject was
	 * retrieved from the platform, it has an ID. If just created, there is no ID.
	 *
	 * @return String the unique identifier of the event or null if not available.
	 */
	public String getId() {
		return (String) anyObject.get("id");
	}

	/**
	 * Get the name of the managed object.
	 *
	 * @return a String with the name or null if not available.
	 */
	public String getName() {
		return (String) anyObject.get("name");
	}

	/**
	 * Setting the name of the managed object. The name is typically a human
	 * readable identifier of the device.
	 *
	 * @param name
	 *            a String with the name.
	 */
	public void setName(String name) {
		anyObject.put("name", name);
	}

	/**
	 * Set the unique identifier of the managed object. Just used internally.
	 *
	 * @param id
	 *            the new identifier.
	 */
	public void setId(String id) {
		anyObject.put("id", id);
	}

	/**
	 * Setting the managed object type.
	 *
	 * @param type
	 *            a String with the managed object type. Use cot_abc_xyz style.
	 */
	public void setType(String type) {
		anyObject.put("type", type);
	}

	/**
	 * Get the type of the managed object. The type categorizes the managed object.
	 *
	 * @return a String with the type or null if not available.
	 */
	public String getType() {
		return (String) anyObject.get("type");
	}

	/**
	 * Add a library to this ManagedObject.
	 *
	 * @param agentFragment Fragment to add.
	 */
	public void addFragment(AgentFragment agentFragment) {
		anyObject.put(agentFragment.getId(), agentFragment.getJson());
	}
}
