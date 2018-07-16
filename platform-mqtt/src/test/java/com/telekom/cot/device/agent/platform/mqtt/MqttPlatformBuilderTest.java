package com.telekom.cot.device.agent.platform.mqtt;

import java.util.Date;
import java.util.Properties;

import org.junit.Test;

public class MqttPlatformBuilderTest {

    @SuppressWarnings("unused")
    @Test
    public void test() throws InterruptedException {
        MqttPlatform mqttPlatform = MqttPlatformBuilder
                        .create("iccid", new Properties())
                        .setVerticalWorkerPoolSize(1).setVertxEventLoopPoolSize(1).setVertxInternalBlockingPoolSize(1)
                        .setVertxWorkerPoolSize(1).build(s -> System.out.println(new Date() + " " + s));
    }
}
