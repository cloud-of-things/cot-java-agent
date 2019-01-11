package com.telekom.cot.device.agent.platform.mqtt;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.platform.mqtt.event.LifecycleResponseAgentEvent;
import com.telekom.cot.device.agent.platform.mqtt.event.LifecycleResponseAgentEvent.Lifecycle;
import com.telekom.cot.device.agent.platform.mqtt.event.PublishedValuesAgentEvent;
import com.telekom.cot.device.agent.platform.mqtt.event.PublishedValuesAgentEventListener;
import com.telekom.cot.device.agent.service.event.AgentContext;
import com.telekom.cot.device.agent.service.event.AgentContextImpl.Execution;
import com.telekom.cot.device.agent.service.event.AgentEvent;
import com.telekom.cot.device.agent.service.event.AgentEventPublisher;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.telekom.cot.device.agent.platform.mqtt.event.PublishedValuesAgentEvent.createEvent;

/**
 * The callback of the MQTT Connector publish.
 *
 */
public class PublishCallback implements Consumer<String> {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishCallback.class);
    /** The MQTT response reader. */
    private TemplateResponseReader reader;
    /** Map the template response to the event class. */
    private Map<TemplateId, Class<? extends PublishedValuesAgentEvent>> monitoredTemplates = new HashedMap<>();
    /** The event publisher. */
    private AgentEventPublisher publisher;
    /** */
    private Map<TemplateId, List<TemplateId>> ignoredTemplates = new HashedMap<>();
    /** */
    private AgentContext agentContext;

    /** The private constructor. */
    private PublishCallback(AgentEventPublisher publisher, AgentContext agentContext, TemplateResponseReader reader) {
        this.reader = reader;
        this.publisher = publisher;
        this.agentContext = agentContext;
    }

    /**
     * Create the PublishCallback instance.
     * 
     * @param publisher
     *            the event publisher
     * @return the instance
     * @throws AbstractAgentException
     */
    public static PublishCallback getInstance(AgentContext agentContext) throws AbstractAgentException {
        try {
            AgentEventPublisher publisher = agentContext.getAgentEventPublisher(Execution.ASYNC);
            URI templateResource = PublishCallback.class.getResource("/templates/mqtt.response.templates").toURI();
            TemplateResponseReader reader = TemplateResponseReaderBuilder.getInstance()
                            .setTemplateResource(templateResource).build();
            return new PublishCallback(publisher, agentContext, reader);
        } catch (IOException | URISyntaxException e) {
            throw new PlatformServiceException("can't create publish callback", e);
        }
    }

    /**
     * Which event is created by which template response.
     * 
     * @param templateId
     *            the template id
     * @param agentEventClass
     *            the event class
     */
    public <T extends PublishedValuesAgentEvent> void monitorResponse(TemplateId templateId, Class<T> agentEventClass) {
        monitoredTemplates.put(templateId, agentEventClass);
    }

    /**
     * Monitor the request by given TemplateId and Listener.
     * 
     * @param templateId
     * @param listener
     * @throws AbstractAgentException
     */
    public <T extends PublishedValuesAgentEvent> void monitorResponse(TemplateId templateId,
                    PublishedValuesAgentEventListener<?, ?> listener) throws AbstractAgentException {
        monitorResponse(templateId, listener.getEventClass());
        if (!agentContext.containsAgentEventListener(listener)) {
            agentContext.addAgentEventListener(listener);
        }
    }

    /**
     * Ignore responses by TemplateId. Exist this template in the response, then the ignored templates are removed.
     * 
     * @param templateId
     * @param ignoreTemplates
     */
    public <T extends PublishedValuesAgentEvent> void ignoredResponses(TemplateId templateId,
                    TemplateId... ignoreTemplates) {
        if (!ignoredTemplates.containsKey(templateId)) {
            ignoredTemplates.put(templateId, new ArrayList<>());
        }
        ignoredTemplates.get(templateId).addAll(Arrays.asList(ignoreTemplates));
    }

    /**
     * Reverse ignore of responses by TemplateIds.
     * 
     * @param templateId
     * @param ignoreTemplates
     */
    public <T extends PublishedValuesAgentEvent> void reverseIgnoredResponses(TemplateId ignoreTemplate,
                    TemplateId... templateIds) {
        for (TemplateId templateId : templateIds) {
            ignoredResponses(templateId, ignoreTemplate);
        }
    }

    /**
     * The Consumer<String> implementation.
     */
    @Override
    public void accept(String response) {
        publisher.publishEvent(new LifecycleResponseAgentEvent(this, Lifecycle.STARTUP));
        LOGGER.debug("response: \n{}", idsToEnums(response));
        // check error codes
        String[] error = readErrorValues(response);
        if (error.length > 0) {
            LOGGER.warn("errors in responses {}", Arrays.asList(error));
        }
        // check valid codes
        String[] lines = readValidValues(response);
        if (lines.length == 0) {
           LOGGER.debug("could not find valid responses");
        } else if (!isValidResponse(lines[0])) {
            LOGGER.debug("valid responses {}", Arrays.asList(lines));
            // read values from response
            Map<TemplateId, List<PublishedValues>> allValues = toMappedPublishedValues(lines);
            for (PublishedValues values : filterIgnoredTemplates(allValues)) {
                TemplateId templateId = values.getTemplateId();
                if (monitoredTemplates.containsKey(templateId)) {
                    Class<? extends PublishedValuesAgentEvent> eventClass = monitoredTemplates.get(templateId);
                    LOGGER.debug("handle response as AgentEvent {} by event {}", templateId, eventClass);
                    AgentEvent event = createEvent(eventClass, this, values);
                    LOGGER.debug("publish event {}", event);
                    publisher.publishEvent(event);
                }
            }
        }
        publisher.publishEvent(new LifecycleResponseAgentEvent(this, Lifecycle.FINISHED));
    }

    /**
     * replace ID's to GET_MANAGED_OBJECT_ID_RES(601)
     * 
     * @param response
     * @return
     */
    private static String idsToEnums(String response) {
        String copy = String.valueOf(response);
        for (TemplateId id : TemplateId.values()) {
            copy = copy.replaceAll(id.getId() + ",", id + "(" + id.getId() + "),");
        }
        return copy;
    }

    private String[] readValidValues(String response) {
        return Arrays.asList(split(response)).stream()
                        // filter all identified responses
                        .filter(l -> TemplateId.isValidRes(l))
                        // collect they
                        .collect(Collectors.toList())
                        // to array
                        .toArray(new String[] {});
    }

    private String[] readErrorValues(String response) {
        return Arrays.asList(split(response)).stream()
                        // filter all identified responses
                        .filter(l -> TemplateId.isErrorRes(l))
                        // collect they
                        .collect(Collectors.toList())
                        // to array
                        .toArray(new String[] {});
    }

    /**
     * Removes all the values ​​listed in the ignore templates.
     * 
     * @param allValues
     * @return
     */
    private List<PublishedValues> filterIgnoredTemplates(Map<TemplateId, List<PublishedValues>> allValues) {
        List<TemplateId> allValuesKeys = new ArrayList<>(allValues.keySet());
        for (TemplateId templateId : allValuesKeys) {
            if (ignoredTemplates.containsKey(templateId)) {
                for (TemplateId ignoredTemplate : ignoredTemplates.get(templateId)) {
                    Object pv = allValues.remove(ignoredTemplate);
                    if (Objects.nonNull(pv)) {
                        LOGGER.debug("remove response {} by ignored templates", ignoredTemplate);
                    }
                }
            }
        }
        return allValues.entrySet().stream().map(e -> e.getValue()).flatMap(l -> l.stream())
                        .collect(Collectors.toList());
    }

    /**
     * Is this an empty response.
     * 
     * @param response
     *            the complete template response
     * @return
     */
    private boolean isValidResponse(String response) {
        return !(Objects.nonNull(response) && response.startsWith("87"));
    }

    /**
     * Read the response values by the TemplateResponseReader.
     * 
     * @param response
     *            the complete template response
     * @return a list of published values
     */
    private Map<TemplateId, List<PublishedValues>> toMappedPublishedValues(String[] content) {
        // check content
        if (content.length < 2) {
            LOGGER.warn("MQTT response has no content");
            return new HashMap<>();
        }
        // check all values
        Map<TemplateId, List<PublishedValues>> result = new HashMap<>();
        for (int index = 1; index < content.length; index++) {
            String templateContent = content[index];
            PublishedValues publishedValues = reader.read(templateContent);
            if (publishedValues.isValid()) {
                LOGGER.debug("VALID response={} publishedValues={}", templateContent, publishedValues);
            } else {
                LOGGER.warn("INVALID response={} publishedValues={}", templateContent, publishedValues);
            }
            if (!result.containsKey(publishedValues.getTemplateId())) {
                result.put(publishedValues.getTemplateId(), new ArrayList<>());
            }
            result.get(publishedValues.getTemplateId()).add(publishedValues);
        }
        return result;
    }

    /**
     * Split the response by a CR and ignore the comma in a double quote area.
     * 
     * @param response
     * @return the template content
     */
    private String[] split(String response) {
        List<String> content = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int doubleQuote = 0;
        for (char value : response.toCharArray()) {
            if (doubleQuote == 0 && value == '\n') {
                content.add(sb.toString());
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
        String value = sb.toString();
        if (Objects.nonNull(value) && value.length() > 0) {
            content.add(value);
        }
        return content.toArray(new String[] {});
    }
}
