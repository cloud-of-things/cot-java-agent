package com.telekom.cot.device.agent.platform.mqtt;

import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tarent.telekom.cot.mqtt.MQTTHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * Encapsulate the Vertx MQTTHelper implementation.
 *
 */
public class MqttPlatformBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttPlatformBuilder.class);
    private int vertxEventLoopPoolSize;
    private int vertxWorkerPoolSize;
    private int vertxInternalBlockingPoolSize;
    private int verticalWorkerPoolSize;
    private Properties prop;
    private String iccId;

    private MqttPlatformBuilder(String iccId, Properties prop) {
        this.prop = prop;
        this.iccId = iccId;
    }

    /**
     * Create the builder.
     * 
     * @param iccId
     * @param prop
     * @return
     */
    public static MqttPlatformBuilder create(String iccId, Properties prop) {
        return new MqttPlatformBuilder(iccId, prop);
    }

    public MqttPlatformBuilder setVertxEventLoopPoolSize(int vertxEventLoopPoolSize) {
        this.vertxEventLoopPoolSize = vertxEventLoopPoolSize;
        return this;
    }

    public MqttPlatformBuilder setVertxWorkerPoolSize(int vertxWorkerPoolSize) {
        this.vertxWorkerPoolSize = vertxWorkerPoolSize;
        return this;
    }

    public MqttPlatformBuilder setVertxInternalBlockingPoolSize(int vertxInternalBlockingPoolSize) {
        this.vertxInternalBlockingPoolSize = vertxInternalBlockingPoolSize;
        return this;
    }

    public MqttPlatformBuilder setVerticalWorkerPoolSize(int verticalWorkerPoolSize) {
        this.verticalWorkerPoolSize = verticalWorkerPoolSize;
        return this;
    }

    /**
     * Deploy the MQTTHelper with the given configurations and return MqttPlatform interface.
     * 
     * @param successfulCallback
     * @return
     */
    public MqttPlatform build(Consumer<Boolean> successfulCallback) {
        VertxOptions options = new VertxOptions();
        if (Objects.nonNull(vertxEventLoopPoolSize)) {
            options.setEventLoopPoolSize(vertxEventLoopPoolSize);
        }
        if (Objects.nonNull(vertxWorkerPoolSize)) {
            options.setWorkerPoolSize(vertxWorkerPoolSize);
        }
        if (Objects.nonNull(vertxInternalBlockingPoolSize)) {
            options.setInternalBlockingPoolSize(vertxInternalBlockingPoolSize);
        }
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        if (Objects.nonNull(verticalWorkerPoolSize)) {
            deploymentOptions.setWorkerPoolSize(verticalWorkerPoolSize);
        }
        Vertx vertx = Vertx.vertx(options);
        MQTTHelper mqttHelper = new MQTTHelper();
        vertx.deployVerticle(mqttHelper, deploymentOptions, h -> {
            if (h.failed()) {
                LOGGER.error("MQTTHelper is not correctly deployed", h.cause());
            } else if (h.succeeded()) {
                LOGGER.info("MQTTHelper is correctly deployed");
            }
            successfulCallback.accept(h.succeeded());
        });
        return new MqttPlatformWrapper(mqttHelper, iccId, prop);
    }

    /**
     * Wrapper of the MQTTHelper.
     *
     */
    private static class MqttPlatformWrapper implements MqttPlatform {

        private MQTTHelper mqttHelper;
        private Properties prop;
        private String iccId;

        private MqttPlatformWrapper(MQTTHelper mqttHelper, String iccId, Properties prop) {
            this.mqttHelper = mqttHelper;
            this.prop = prop;
            this.iccId = iccId;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Properties getProperties() {
            return prop;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void registerDevice(java.util.function.Consumer<String> callback) {
            this.mqttHelper.registerDevice(iccId, prop, callback);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void publishMessage(String message, java.util.function.Consumer<Boolean> callback) {
            this.mqttHelper.publishMessage(iccId, message, prop, callback);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void subscribeToTopic(java.util.function.Consumer<Object> subscriptionCallback,
                        java.util.function.Consumer<String> callback) {
            this.mqttHelper.subscribeToTopic(iccId, prop, subscriptionCallback, callback);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void unsubscribeFromTopic(java.util.function.Consumer<Boolean> unsubscriptionCallback) {
            this.mqttHelper.unsubscribeFromTopic(iccId, prop, unsubscriptionCallback);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close(Handler<AsyncResult<Void>> completionHandler) {
            mqttHelper.getVertx().close(completionHandler);
        }
    }
}
