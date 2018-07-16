package com.telekom.cot.device.agent.demo.system;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.common.configuration.Configuration;
import com.telekom.cot.device.agent.system.properties.HardwareProperties;

@ConfigurationPath("agent.properties.system.hardware")
public class DemoHardwareProperties extends HardwareProperties implements Configuration {
}
