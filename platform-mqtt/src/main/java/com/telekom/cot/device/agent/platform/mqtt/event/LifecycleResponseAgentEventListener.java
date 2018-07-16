package com.telekom.cot.device.agent.platform.mqtt.event;

import java.util.ArrayList;
import java.util.List;

import com.telekom.cot.device.agent.platform.mqtt.event.LifecycleResponseAgentEvent.Lifecycle;
import com.telekom.cot.device.agent.service.event.AgentEventListener;

public class LifecycleResponseAgentEventListener implements AgentEventListener<LifecycleResponseAgentEvent> {

    private final List<FinishedResponseListener> finishedListeners = new ArrayList<>();
    private final List<StartupResponseListener> startupListeners = new ArrayList<>();

    @Override
    public void onAgentEvent(LifecycleResponseAgentEvent event) {
        if (event.getLifecycle() == Lifecycle.FINISHED) {
            for (FinishedResponseListener listener : finishedListeners) {
                listener.finished();
            }
        } else if (event.getLifecycle() == Lifecycle.STARTUP) {
            for (StartupResponseListener listener : startupListeners) {
                listener.startup();
            }
        }
    }

    public void addFinishedListener(FinishedResponseListener listener) {
        finishedListeners.add(listener);
    }

    public void addStartupListener(StartupResponseListener listener) {
        startupListeners.add(listener);
    }

    interface FinishedResponseListener {

        public void finished();
    }

    interface StartupResponseListener {

        public void startup();
    }
}
