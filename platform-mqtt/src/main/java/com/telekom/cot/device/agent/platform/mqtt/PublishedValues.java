package com.telekom.cot.device.agent.platform.mqtt;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the value of the template response.
 */
public class PublishedValues {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishedValues.class);
    /** The values belongs to the template. */
    private TemplateId templateId;
    /** The mapped values. */
    private Map<String, String> values;
    /** The error state. */
    private boolean error = false;

    public PublishedValues(TemplateId templateId, String[] attributes, String[] values) {
        this.templateId = templateId;
        this.values = joinToMap(attributes, values);
    }

    /**
     * Get TemplateId.
     * 
     * @return
     */
    public TemplateId getTemplateId() {
        return templateId;
    }

    /**
     * Get specific mapped value.
     * 
     * @param attribute
     *            the JSON path
     * @return the response value
     */
    public String getValue(String attribute) {
        if (contains(attribute)) {
            return values.get(attribute);
        }
        throw new IllegalArgumentException("attribute doesn't exist " + attribute);
    }

    /**
     * Contains a value to the specific value.
     * 
     * @param attribute
     * @return
     */
    public boolean contains(String attribute) {
        return values.containsKey(attribute);
    }

    /**
     * The size of the mapped values.
     * 
     * @return
     */
    public Object size() {
        return values.size();
    }

    /**
     * Is this a correct value.
     * 
     * @return
     */
    public boolean isValid() {
        return templateId != TemplateId.NOT_AVAILABLE && !error;
    }

    private Map<String, String> joinToMap(String[] attributes, String[] values) {
        LOGGER.debug("join values {} by {}", attributes, values);
        Map<String, String> result = new HashMap<>();
        for (int index = 0; index < values.length; index++) {
            String attr = "unknown" + index;
            if (Objects.nonNull(attributes) && index < attributes.length) {
                attr = attributes[index];
            } else if (!error) {
                error = true;
            }
            result.put(attr, values[index]);
        }
        return result;
    }

    @Override
    public String toString() {
        return "PublishedValues [templateId=" + templateId + ", values=" + values + "]";
    }
}
