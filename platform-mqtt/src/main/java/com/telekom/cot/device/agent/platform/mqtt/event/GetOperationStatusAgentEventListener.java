package com.telekom.cot.device.agent.platform.mqtt.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.platform.mqtt.event.LifecycleResponseAgentEventListener.FinishedResponseListener;
import com.telekom.cot.device.agent.platform.mqtt.event.LifecycleResponseAgentEventListener.StartupResponseListener;

public class GetOperationStatusAgentEventListener
                extends PublishedValuesAgentEventListener<GetOperationStatus, GetOperationStatusAgentEvent>
                implements FinishedResponseListener, StartupResponseListener {

    private List<GetOperationStatus> businessObjects = new ArrayList<>();
    private AtomicBoolean finished = new AtomicBoolean(false);

    @Override
    void handle(GetOperationStatus businessObject) {
        businessObjects.add(businessObject);
    }

    public List<GetOperationStatus> waitOnAllAgentEvents() throws InterruptedException {
        while(!finished.get()) {
            TimeUnit.MILLISECONDS.sleep(500);
        }
        return new ArrayList<>(businessObjects);
    }

    public GetOperationStatus create(PublishedValues publishedValues) {
        String id = publishedValues.getValue("id");
        String status = publishedValues.getValue("status");
        return new GetOperationStatus(id, status);
    }

    @Override
    public void finished() {
        finished.set(true);
    }
    
    @Override
    public void startup() {
        finished.set(false);
        businessObjects.clear();
    }
    
    @Override
    public Class<GetOperationStatusAgentEvent> getEventClass() {
        return GetOperationStatusAgentEvent.class;
    }
}
