package com.telekom.cot.device.agent.platform.mqtt;

import java.util.Properties;
import java.util.function.Consumer;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface MqttPlatform {

    /**
     * Registers the given deviceId (iccID) with the given Properties. The result is then sent back with the given
     * callback once/if it's retrieved from the server.
     * 
     * @param callback
     */
    public void registerDevice(final Consumer<String> callback);

    /**
     * Publishes a given message on the given topic with the given Properties. The result is then sent back with the
     * given callback once/if it's retrieved from the server.
     * 
     * @param message
     * @param callback
     */
    public void publishMessage(final String message, final Consumer<Boolean> callback);

    /**
     * Subscribes to a given topic with the given Properties.
     * 
     * @param subscriptionCallback
     * @param callback
     */
    public void subscribeToTopic(final Consumer<Object> subscriptionCallback, final Consumer<String> callback);

    /**
     * Unsubscribes to a given topic with the given Properties.
     * 
     * @param unsubscriptionCallback
     */
    public void unsubscribeFromTopic(final Consumer<Boolean> unsubscriptionCallback);

    /**
     * Close the Vertx deployed Verticle's.
     * 
     * @param completionHandler
     */
    public void close(Handler<AsyncResult<Void>> completionHandler);

    /**
     * The configuration parameter.
     * 
     * @return
     */
    public Properties getProperties();
}
