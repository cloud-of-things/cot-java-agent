package com.telekom.cot.device.agent.platform.mqtt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read the MQTT response and map the values to the JSON path of the response template. Example: MQTT response
 * 87,1,template 501,2,1234,SUCCESSFUL PublishedValues managedObject.id=1234 status=SUCCESSFUL
 *
 */
public class TemplateResponseReader {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateResponseReader.class);
    /** The assigned attributes to the TemplateId. */
    private Map<TemplateId, String[]> templateAttributes;

    private TemplateResponseReader(Map<TemplateId, String[]> templateAttributes) {
        this.templateAttributes = templateAttributes;
        this.templateAttributes.put(TemplateId.NOT_AVAILABLE, new String[] {});
    }

    /**
     * Create a reader instance.
     * 
     * @param templateAttributes
     * @return
     */
    public static TemplateResponseReader getInstance(Map<TemplateId, String[]> templateAttributes) {
        return new TemplateResponseReader(templateAttributes);
    }

    /**
     * Read the value from the response to a PublishedValues instance.
     * 
     * @param response
     * @return
     */
    public PublishedValues read(String response) {
        if (Objects.isNull(response)) {
            LOGGER.warn("resonse is null");
            return new PublishedValues(TemplateId.NOT_AVAILABLE, new String[] {}, new String[] {});
        }
        String[] allValues = split(response);
        if (allValues.length < 2) {
            return new PublishedValues(TemplateId.NOT_AVAILABLE, new String[] {}, new String[] {});
        }
        TemplateId templateId = TemplateId.findByResId(allValues[0]);
        String[] values = new String[allValues.length - 2];
        String[] attributes = templateAttributes.get(templateId);
        // read the value from the second index
        System.arraycopy(allValues, 2, values, 0, values.length);
        return new PublishedValues(templateId, attributes, values);
    }

    /**
     * Split the response by a comma and ignore the comma in a double quote area.
     * 
     * @param response
     * @return the template content
     */
    private static String[] split(String response) {
        List<String> contents = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int doubleQuote = 0;
        for (char value : response.toCharArray()) {
            if (doubleQuote == 0 && value == ',') {
                add(contents, sb);
                sb = new StringBuilder();
            } else if (doubleQuote % 2 == 0 && value == '"') {
                sb.append(value);
                doubleQuote++;
            } else if (doubleQuote % 2 != 0 && value == '"') {
                sb.append(value);
                doubleQuote--;
            } else {
                sb.append(value);
            }
        }
        add(contents, sb);
        return contents.toArray(new String[] {});
    }

    /**
     * Add the content to the list.
     * 
     * @param contents
     * @param sb
     */
    private static void add(List<String> contents, StringBuilder sb) {
        String content = sb.toString();
        if (Objects.nonNull(content) && content.length() > 0) {
            if (content.indexOf("\"") == 0) {
                content = content.substring(1);
            }
            if (content.lastIndexOf("\"") == content.length() - 1) {
                content = content.substring(0, content.length() - 1);
            }
        }
        contents.add(content);
    }
}
