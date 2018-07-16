package com.telekom.cot.device.agent.platform.mqtt;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.platform.mqtt.event.GetOperationStatus;
import com.telekom.cot.device.agent.platform.mqtt.event.GetOperationStatusAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.LifecycleResponseAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.OperationTestOperationAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.PublishedValuesAgentEvent;
import com.telekom.cot.device.agent.platform.mqtt.event.PublishedValuesAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.StartupAgentEventListener;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;
import com.telekom.cot.device.agent.service.event.AgentContextImpl;
import com.telekom.cot.device.agent.service.event.AgentEventPublisher;

public class PublishCallbackTest {

    @Mock
    private AgentEventPublisher mockAgentEventPublisher;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMonitorResponseByListener() throws AbstractAgentException, InterruptedException, JsonParseException,
                    JsonMappingException, IOException {
        // create PublishCallback
        AgentContextImpl agentContext = new AgentContextImpl();
        PublishCallback callback = PublishCallback.getInstance(agentContext);
        // event
        OpConfUpdateListener listener = new OpConfUpdateListener();
        callback.monitorResponse(TemplateId.UPDATE_SOFTWARE_LIST_RES, listener);
        callback.monitorResponse(TemplateId.UPDATE_CONFIGURATION_RES, listener);
        // check agent context
        assertThat(agentContext.containsAgentEventListener(listener), Matchers.equalTo(true));
        agentContext.removeAgentEventListener(listener);
        assertThat(agentContext.containsAgentEventListener(listener), Matchers.equalTo(false));
    }

    @Test
    public void testConfigurationByMonitorResponseAndListener() throws AbstractAgentException, InterruptedException,
                    JsonParseException, JsonMappingException, IOException {
        // get config response content
        InputStream inputStream = PublishCallbackTest.class.getResourceAsStream("/configuration.response");
        String response = new BufferedReader(new InputStreamReader(inputStream)).lines()
                        .reduce((l1, l2) -> String.valueOf(l1 + '\n' + l2)).get();
        // create PublishCallback
        AgentContextImpl agentContext = new AgentContextImpl();
        PublishCallback callback = PublishCallback.getInstance(agentContext);
        // event
        OpConfUpdateListener listener = new OpConfUpdateListener();
        callback.monitorResponse(TemplateId.OPERATION_CONFIGURATION_RES, listener);
        // execute functional interface after one second
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                callback.accept(response);
            }
        }, 1000);
        String conf = listener.waitOnAgentEventAndCreate();
        // check
        assertThat(conf.length(), Matchers.equalTo(1554));
        YAMLMapper om = new YAMLMapper();
        om.readValue(conf.getBytes(), Map.class);
    }

    @Test
    public void testConfigurationByMonitorResponseAndEventClass() throws AbstractAgentException, InterruptedException,
                    JsonParseException, JsonMappingException, IOException {
        // get config response content
        InputStream inputStream = PublishCallbackTest.class.getResourceAsStream("/configuration.response");
        String response = new BufferedReader(new InputStreamReader(inputStream)).lines()
                        .reduce((l1, l2) -> String.valueOf(l1 + '\n' + l2)).get();
        // create PublishCallback
        AgentContextImpl agentContext = new AgentContextImpl();
        PublishCallback callback = PublishCallback.getInstance(agentContext);
        // event
        OpConfUpdateListener listener = new OpConfUpdateListener();
        agentContext.addAgentEventListener(listener);
        callback.monitorResponse(TemplateId.OPERATION_CONFIGURATION_RES, OpConfUpdateEvent.class);
        // execute functional interface  after one second
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                callback.accept(response);
            }
        }, 1000);
        String conf = listener.waitOnAgentEventAndCreate();
        // check
        assertThat(conf.length(), Matchers.equalTo(1554));
        YAMLMapper om = new YAMLMapper();
        om.readValue(conf.getBytes(), Map.class);
    }

    @Test
    public void testConfigurationEmpty() throws AbstractAgentException, InterruptedException, JsonParseException,
                    JsonMappingException, IOException {
        // create PublishCallback
        AgentContextImpl agentContext = new AgentContextImpl();
        PublishCallback callback = PublishCallback.getInstance(agentContext);
        // event
        callback.monitorResponse(TemplateId.OPERATION_CONFIGURATION_RES, OpConfUpdateEvent.class);
        OpConfUpdateListener listener = new OpConfUpdateListener();
        agentContext.addAgentEventListener(listener);
        // execute functional interface
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                callback.accept("87,2,novaMqttTemplates04\n512,2,19794459,EXECUTING,");
            }
        }, 1000);
        String conf = listener.waitOnAgentEventAndCreate();
        // check
        assertThat(conf.length(), Matchers.equalTo(0));
    }

    @Test
    public void testOperationNotValid() throws AbstractAgentException, InterruptedException {
        String response = "87,1,novaMqttTemplatesOps04\n" + //
                        "511,2,19330625,EXECUTING,SUCCESSFUL,notexist\n";
        // prepare callback
        AgentContextImpl agentContext = new AgentContextImpl();
        PublishCallback callback = PublishCallback.getInstance(agentContext);
        // prepare listener
        ConcurrentLinkedQueue<Operation> pendingOperations = new ConcurrentLinkedQueue<>();
        OperationTestOperationAgentEventListener listener = new OperationTestOperationAgentEventListener(
                        pendingOperations);
        // add listener to template
        callback.monitorResponse(TemplateId.OPERATION_TEST_RES, listener);
        // execute test
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                callback.accept(response);
            }
        }, 1000);
        Operation operation = listener.waitOnAgentEventAndCreate();
        // check operation equals null
        assertThat(operation, Matchers.nullValue());
    }

    @Test
    public void testOperationValid() throws AbstractAgentException, InterruptedException {
        // prepare response
        String response = "87,1,novaMqttTemplatesOps04\n" + //
                        "511,2,19330625,EXECUTING,SUCCESSFUL\n";
        // prepare callback
        AgentContextImpl agentContext = new AgentContextImpl();
        PublishCallback callback = PublishCallback.getInstance(agentContext);
        // prepare listener
        ConcurrentLinkedQueue<Operation> pendingOperations = new ConcurrentLinkedQueue<>();
        OperationTestOperationAgentEventListener listener = new OperationTestOperationAgentEventListener(
                        pendingOperations);
        callback.monitorResponse(TemplateId.OPERATION_TEST_RES, listener);
        // execute test
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                callback.accept(response);
            }
        }, 1000);
        Operation operation = listener.waitOnAgentEventAndCreate();
        // check business object
        assertThat(operation.getId(), Matchers.equalTo("19330625"));
        assertThat(operation.getProperty("status", OperationStatus.class),
                        // status must be EXECUTING
                        Matchers.equalTo(OperationStatus.EXECUTING));
        assertThat(operation.getProperty("c8y_TestOperation", Map.class).get("givenStatus"),
                        // given status must be SUCCESSFUL
                        Matchers.equalTo("SUCCESSFUL"));
    }

    @Test
    public void testGetOperationsStatusByNotEmpty() throws AbstractAgentException, InterruptedException {
        // empty response
        String response = "87,1,novaMqttTemplatesOps04\n" + //
                        "210,2,19330625,EXECUTING\n" + "210,2,19330626,SUCCESSFUL\n";
        // prepare callback
        AgentContextImpl agentContext = new AgentContextImpl();
        PublishCallback callback = PublishCallback.getInstance(agentContext);
        // prepare lifecycle
        LifecycleResponseAgentEventListener lifecycle = new LifecycleResponseAgentEventListener();
        agentContext.addAgentEventListener(lifecycle);
        // prepare listener
        GetOperationStatusAgentEventListener listener = new GetOperationStatusAgentEventListener();
        // could be previous result (must be removed by lifecycle)
        List<GetOperationStatus> businessObjects = new ArrayList<>();
        businessObjects.add(new GetOperationStatus("1", "status"));
        InjectionUtil.inject(listener, businessObjects);
        // add lifecycle listener
        lifecycle.addFinishedListener(listener);
        lifecycle.addStartupListener(listener);
        callback.monitorResponse(TemplateId.STATUS_OF_OPERATION_RESTART_RES, listener);
        // execute test
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                callback.accept(response);
            }
        }, 1000);
        List<GetOperationStatus> ops = listener.waitOnAllAgentEvents();
        // check
        assertEquals(2, ops.size());
        assertThat(ops.get(0).getId(), Matchers.equalTo("19330625"));
        assertThat(ops.get(0).getStatus(), Matchers.equalTo("EXECUTING"));
        assertThat(ops.get(1).getId(), Matchers.equalTo("19330626"));
        assertThat(ops.get(1).getStatus(), Matchers.equalTo("SUCCESSFUL"));
    }

    @Test
    public void testGetOperationStatusByEmpty() throws AbstractAgentException, InterruptedException {
        // empty response
        String response = "";
        // prepare callback
        AgentContextImpl agentContext = new AgentContextImpl();
        PublishCallback callback = PublishCallback.getInstance(agentContext);
        // prepare lifecycle
        LifecycleResponseAgentEventListener lifecycle = new LifecycleResponseAgentEventListener();
        agentContext.addAgentEventListener(lifecycle);
        // prepare listener
        GetOperationStatusAgentEventListener listener = new GetOperationStatusAgentEventListener();
        // could be previous result (must be removed by lifecycle)
        List<GetOperationStatus> businessObjects = new ArrayList<>();
        businessObjects.add(new GetOperationStatus("1", "status"));
        InjectionUtil.inject(listener, businessObjects);
        // add lifecycle listener
        lifecycle.addFinishedListener(listener);
        lifecycle.addStartupListener(listener);
        callback.monitorResponse(TemplateId.STATUS_OF_OPERATION_RESTART_RES, listener);
        // execute test
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                callback.accept(response);
            }
        }, 1000);
        List<GetOperationStatus> ops = listener.waitOnAllAgentEvents();
        // check
        assertEquals(0, ops.size());
    }

    @Test
    public void testEvent_EVENT_STARTUP_RES() throws AbstractAgentException, InterruptedException {
        // prepare callback
        AgentContextImpl agentContext = new AgentContextImpl();
        PublishCallback callback = PublishCallback.getInstance(agentContext);
        // listener
        StartupAgentEventListener startupAEL = new StartupAgentEventListener();
        callback.monitorResponse(TemplateId.EVENT_STARTUP_RES, startupAEL);
        // execute test
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                callback.accept("87,1,novaMqttTemplatesOps04\n401,1,18106509");
            }
        }, 1000);
        String id = startupAEL.waitOnAgentEventAndCreate();
        assertThat(id, Matchers.equalTo("18106509"));
    }
    
    @Test
    public void testErrorCodesBy_EVENT_STARTUP_RES() throws AbstractAgentException, InterruptedException {
        // prepare callback
        AgentContextImpl agentContext = new AgentContextImpl();
        PublishCallback callback = PublishCallback.getInstance(agentContext);
        // listener
        StartupAgentEventListener startupAEL = new StartupAgentEventListener();
        callback.monitorResponse(TemplateId.EVENT_STARTUP_RES, startupAEL);
        // execute test
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                callback.accept("42,4,Malformed request\n87,2,novaMqttTemplates04\n401,1,18106509");
            }
        }, 1000);
        String id = startupAEL.waitOnAgentEventAndCreate();
        assertThat(id, Matchers.equalTo("18106509"));
    }

    public static class OpConfUpdateEvent extends PublishedValuesAgentEvent {

        public OpConfUpdateEvent(PublishCallback source, PublishedValues publishedValues) {
            super(source, publishedValues);
        }
    }

    public static class OpConfUpdateListener extends PublishedValuesAgentEventListener<String, OpConfUpdateEvent> {

        public String create(PublishedValues publishedValues) {
            return publishedValues.getValue("c8y_Configuration.config");
        }

        @Override
        public Class<OpConfUpdateEvent> getEventClass() {
            return OpConfUpdateEvent.class;
        }
    }
}
