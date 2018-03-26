package com.telekom.cot.device.agent.operation.handler;

import static org.mockito.Mockito.*;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.telekom.cot.device.agent.common.ChecksumAlgorithm;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.operation.softwareupdate.SoftwareUpdateConfig;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;
import com.telekom.m2m.cot.restsdk.util.ExtensibleObject;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OperationExecuteBuilder.class)
public class AgentOperationsHandlerServiceTest {

    private AgentOperationsHandlerService agentOperationsHandlerService;
    private AgentOperationsHandlerConfiguration config;
    private Operation testOperation;
    private Operation confOperation;
    private Operation softwareOperation;

    @Mock
    private AgentServiceProvider mockServiceProvider;
    @Mock
    private ConfigurationManager mockConfigurationManager;
    @Mock
    private OperationExecuteBuilder mockOperationExecuteBuilder;
    @Mock
    private PlatformService mockPlatformService;
    
    @SuppressWarnings("rawtypes")
	@Mock
    private OperationExecute mockOperationExecute;

    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws AbstractAgentException {

        MockitoAnnotations.initMocks(this);

        // service
        agentOperationsHandlerService = new AgentOperationsHandlerService();
        // conf
        config = new AgentOperationsHandlerConfiguration();
        TestOperationConfig demoOperationConfig = new TestOperationConfig();
        demoOperationConfig.setDelay(1);
        config.setTestOperation(demoOperationConfig);
        config.setSoftwareUpdate(new SoftwareUpdateConfig());
        config.getSoftwareUpdate().setChecksumAlgorithm(ChecksumAlgorithm.MD5);

        // demo operation
        testOperation = new Operation();
        ExtensibleObject demoOperationEx = new ExtensibleObject();
        demoOperationEx.set("givenStatus", "GIVEN_SUCCESSFUL");
        testOperation.set("c8y_TestOperation", testOperation);
        // conf operation
        confOperation = new Operation();
        ExtensibleObject confContentOperation = new ExtensibleObject();
        confContentOperation.set("config", "agent.yaml");
        confOperation.set("c8y_Configuration", confContentOperation);
        // software operation
        softwareOperation = new Operation();
        softwareOperation.set("c8y_SoftwareList", "[]");
        
        // inject mocks
        InjectionUtil.inject(agentOperationsHandlerService, mockServiceProvider);
        InjectionUtil.inject(agentOperationsHandlerService, mockConfigurationManager);

        // static behavior
        PowerMockito.mockStatic(OperationExecuteBuilder.class);
        PowerMockito.when(OperationExecuteBuilder.create(Mockito.any())).thenReturn(mockOperationExecuteBuilder);

        // behavior
        when(mockServiceProvider.getService(PlatformService.class)).thenReturn(mockPlatformService);
        when(mockOperationExecuteBuilder.setExecutorClass(Mockito.any())).thenReturn(mockOperationExecuteBuilder);
        when(mockOperationExecuteBuilder.addParameter(Mockito.any(), Mockito.any())).thenReturn(mockOperationExecuteBuilder);
        when(mockOperationExecuteBuilder.setParameters(Mockito.any())).thenReturn(mockOperationExecuteBuilder);
        when(mockOperationExecuteBuilder.setCallback(Mockito.any())).thenReturn(mockOperationExecuteBuilder);
        when(mockOperationExecuteBuilder.setConfiguration(Mockito.any())).thenReturn(mockOperationExecuteBuilder);
        when(mockConfigurationManager.getConfiguration(AgentOperationsHandlerConfiguration.class)).thenReturn(config);
        when(mockOperationExecuteBuilder.build()).thenReturn(mockOperationExecute);
    }

    @Test
    public void testStartAndExecuteDemoOperation() throws AbstractAgentException {
        // given perform status SUCCESSFUL
        when(mockOperationExecuteBuilder.setConfiguration(Mockito.any())).thenReturn(mockOperationExecuteBuilder);
        when(mockOperationExecute.perform()).thenReturn(OperationStatus.SUCCESSFUL);
        // when start
        agentOperationsHandlerService.start();
        // and execute
        OperationStatus status = agentOperationsHandlerService.execute(testOperation);
        // then status SUCCESSFUL
        Assert.assertThat(status, Matchers.equalTo(OperationStatus.SUCCESSFUL));
    }

    @Test
    public void testStartAndExecuteConfOperation() throws AbstractAgentException {
        // given perform status SUCCESSFUL
        when(mockOperationExecute.perform()).thenReturn(OperationStatus.SUCCESSFUL);
        // when start
        agentOperationsHandlerService.start();
        // and execute
        OperationStatus status = agentOperationsHandlerService.execute(confOperation);
        // then status SUCCESSFUL
        Assert.assertThat(status, Matchers.equalTo(OperationStatus.SUCCESSFUL));
    }

    @Test
    public void testStartAndExecuteConfOperationException() throws AbstractAgentException {
        // given perform status SUCCESSFUL
        when(mockOperationExecute.perform()).thenThrow(new AgentOperationHandlerException("test"));
        // when start
        agentOperationsHandlerService.start();
        // and execute
        try {
            agentOperationsHandlerService.execute(confOperation);
            Assert.fail();
        } catch (AbstractAgentException exc) {

        }

    }

    @Test
    public void testGetSupportedOperations() {
        Assert.assertThat(agentOperationsHandlerService.getSupportedOperations(), //
                Matchers.equalTo(new String[] { "c8y_TestOperation", "c8y_Configuration", "c8y_SoftwareList" }));
    }
    
    @Test
    public void testStartAndExecuteSoftwarelistOperation() throws AbstractAgentException {
        // given perform status SUCCESSFUL
        when(mockOperationExecuteBuilder.setConfiguration(Mockito.any())).thenReturn(mockOperationExecuteBuilder);
        when(mockOperationExecute.perform()).thenReturn(OperationStatus.SUCCESSFUL);
        // when start
        agentOperationsHandlerService.start();
        // and execute
        OperationStatus status = agentOperationsHandlerService.execute(softwareOperation);
        // then status SUCCESSFUL
        Assert.assertThat(status, Matchers.equalTo(OperationStatus.SUCCESSFUL));
    }
}
