package com.telekom.cot.device.agent.platform.mqtt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The builder create {@code TemplateResponseReader} based on the template resource.
 *
 */
public class TemplateResponseReaderBuilder {

    private URI templateResource;

    private TemplateResponseReaderBuilder() {
    }

    public static TemplateResponseReaderBuilder getInstance() {
        return new TemplateResponseReaderBuilder();
    }

    public TemplateResponseReaderBuilder setTemplateResource(URI templateResource) {
        this.templateResource = templateResource;
        return this;
    }

    public TemplateResponseReader build() throws IOException {
        return TemplateResponseReader.getInstance(read());
    }

    /**
     * Parse the MQTT response templates and map the attributes to TemplateId.
     * 
     * @return
     * @throws IOException
     */
    private Map<TemplateId, String[]> read() throws IOException {
        InputStream inputStream = templateResource.toURL().openStream();
        return new BufferedReader(new InputStreamReader(inputStream))
                        /** read all lines */
                        .lines()
                        /** */
                        .filter(line -> line.startsWith("11"))
                        /** map this to a Template */
                        .map(line -> new Template(line))
                        /** create a map of TemplateId and */
                        .collect(Collectors.toMap(
                                        /** */
                                        Template::getTemplateId,
                                        /** */
                                        Template::getAttributes));
    }

    /**
     * Represents the relation between the Template and the attributes. 
     */
    private static class Template {

        private static final String REGEX_VALID_ID = "(\\$)(\\{)([A-Z]|_){1,50}(\\})";
        private static final Pattern PATTERN = Pattern.compile(REGEX_VALID_ID);
        private TemplateId templateId;
        private String[] attributes;

        private Template(String line) {
            Matcher matcher = PATTERN.matcher(line);
            String templateName = "";
            if (matcher.find()) {
                templateName = line.substring(matcher.start(), matcher.end());
                templateName = clear(templateName, new char[] { '$', '{' }, new char[] { '}' });
            }
            templateId = TemplateId.findByResName(templateName);
            String[] content = split(line);
            if (content.length > 4) {
                attributes = new String[content.length - 4];
                System.arraycopy(content, 4, attributes, 0, content.length - 4);
            }
        }

        private TemplateId getTemplateId() {
            return templateId;
        }

        private String[] getAttributes() {
            return attributes;
        }

        private String[] split(String line) {
            List<String> result = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            int comma = 0;
            int doubleQuotes = 0;
            char[] starts = new char[] { '"', '$', '@', '{', '.' };
            char[] ends = new char[] { '"', '}' };
            for (Character c : line.toCharArray()) {
                if (((doubleQuotes % 2) == 0) && c == ',') {
                    comma++;
                } else if (c == '"') {
                    doubleQuotes++;
                }
                if (comma == 1) {
                    result.add(clear(sb.toString(), starts, ends));
                    sb = new StringBuilder();
                    comma = 0;
                    doubleQuotes = 0;
                } else {
                    sb.append(c);
                }
            }
            result.add(clear(sb.toString(), starts, ends));
            return result.toArray(new String[] {});
        }

        private String clear(String value, char[] starts, char[] ends) {
            value = value.trim();
            for (char remove : starts) {
                value = value.startsWith(String.valueOf(remove)) ? value.substring(1) : value;
            }
            for (char remove : ends) {
                value = value.endsWith(String.valueOf(remove)) ? value.substring(0, value.length() - 1) : value;
            }
            return value;
        }
    }
}
