package com.telekom.cot.device.agent.service.configuration;

import com.telekom.cot.device.agent.system.properties.MobileProperties;

@ConfigurationPath("agent.properties.system.mobile")
public class MobilePropertiesConfiguration extends MobileProperties implements Configuration {

    public MobilePropertiesConfiguration() {
        super();
    }

    public MobilePropertiesConfiguration(String imei, String cellId, String iccid) {
        super(imei, cellId, iccid);
    }
}
