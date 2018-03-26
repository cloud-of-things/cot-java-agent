package com.telekom.cot.device.agent.operation.handler;

import com.telekom.cot.device.agent.common.ChecksumAlgorithm;
import com.telekom.cot.device.agent.common.util.ValidationUtil;
import com.telekom.cot.device.agent.operation.softwareupdate.SoftwareUpdateConfig;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AgentOperationsHandlerConfigurationAndDemoOperationConfigTest {

    private TestOperationConfig demoOperationConfig;
    private SoftwareUpdateConfig softwareUpdateConfig;
    private AgentOperationsHandlerConfiguration agentOperationsHandlerConfiguration;

    @Before
    public void setup() {
        demoOperationConfig = new TestOperationConfig();
        softwareUpdateConfig = new SoftwareUpdateConfig();
        softwareUpdateConfig.setChecksumAlgorithm(ChecksumAlgorithm.MD5);
        agentOperationsHandlerConfiguration = new AgentOperationsHandlerConfiguration();
    }

    @Test
    public void testAndSetters() {
        demoOperationConfig.setDelay(1);
        agentOperationsHandlerConfiguration.setTestOperation(demoOperationConfig);

        Assert.assertEquals(1, demoOperationConfig.getDelay());
        Assert.assertEquals(demoOperationConfig, agentOperationsHandlerConfiguration.getTestOperation());
    }

    @Test
    public void demoOperationIntervalNullTest() {
        Assert.assertFalse(ValidationUtil.isValid(demoOperationConfig));
    }

    @Test
    public void demoOperationIntervalNegativeTest() {
        demoOperationConfig.setDelay(-1);
        Assert.assertFalse(ValidationUtil.isValid(demoOperationConfig));
    }

    @Test
    public void agentOperationsHandlerConfigurationTest() {
        demoOperationConfig.setDelay(1);
        agentOperationsHandlerConfiguration.setTestOperation(demoOperationConfig);
        agentOperationsHandlerConfiguration.setSoftwareUpdate(softwareUpdateConfig);
        Assert.assertEquals(demoOperationConfig, agentOperationsHandlerConfiguration.getTestOperation());
        Assert.assertTrue(ValidationUtil.isValid(agentOperationsHandlerConfiguration));
    }

    @Test
    public void agentOperationsHandlerConfigurationNullTest() {
        Assert.assertFalse(ValidationUtil.isValid(agentOperationsHandlerConfiguration));
    }

    @Test
    public void agentOperationsHandlerConfigurationInvalidTest() {
        demoOperationConfig.setDelay(-1);
        agentOperationsHandlerConfiguration.setTestOperation(demoOperationConfig);
        Assert.assertFalse(ValidationUtil.isValid(agentOperationsHandlerConfiguration));
    }

}
