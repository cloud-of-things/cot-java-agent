package com.telekom.cot.device.agent.system.properties;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.common.configuration.Configuration;

@ConfigurationPath("agent.properties.system.mobile")
public class MobilePropertiesConfiguration extends MobileProperties implements Configuration {

    public MobilePropertiesConfiguration() {
        super();
    }

    public MobilePropertiesConfiguration(String imei, String cellId, String iccid) {
        super(imei, cellId, iccid);
    }
}
