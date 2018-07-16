package com.telekom.cot.device.agent.raspbian.sensor;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManagerImpl;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;

public class CpuTemperatureSensorConfigurationTest {

    @Test
    @SuppressWarnings("unused")
    public void test() throws AbstractAgentException {
        Path agentConf = Paths.get(System.getProperty("user.dir"), "assembly","configuration","agent.yaml");
        ConfigurationManager configurationManager = ConfigurationManagerImpl.getInstance(agentConf);
        CpuTemperatureSensorConfiguration conf = configurationManager.getConfiguration(CpuTemperatureSensorConfiguration.class);
    }

}
