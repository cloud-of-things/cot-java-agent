package com.telekom.cot.device.agent.demo.system;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.common.configuration.Configuration;
import com.telekom.cot.device.agent.system.properties.FirmwareProperties;

@ConfigurationPath("agent.properties.system.firmware")
public class DemoFirmwareProperties extends FirmwareProperties implements Configuration{

}
