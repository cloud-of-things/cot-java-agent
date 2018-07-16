package com.telekom.cot.device.agent.operation.handler;

import static org.mockito.Mockito.when;

import java.util.HashMap;

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
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.operation.softwareupdate.SoftwareUpdateConfig;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;
import com.telekom.cot.device.agent.service.AgentServiceProvider;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OperationExecuteBuilder.class)
public class AgentOperationsHandlerServiceTest {

    private AgentOperationsHandlerService agentOperationsHandlerService;
    private AgentOperationsHandlerConfiguration configuration;
    private Operation testOperation;
    private Operation confOperation;
    private Operation softwareOperation;

    @Mock
    private ConfigurationManager mockConfigurationManager;
    @Mock
    private AgentServiceProvider mockServiceProvider;
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
        configuration = new AgentOperationsHandlerConfiguration();
        TestOperationConfig demoOperationConfig = new TestOperationConfig();
        demoOperationConfig.setDelay(1);
        configuration.setTestOperation(demoOperationConfig);
        configuration.setSoftwareUpdate(new SoftwareUpdateConfig());
        configuration.getSoftwareUpdate().setChecksumAlgorithm(ChecksumAlgorithm.MD5);
        InjectionUtil.inject(agentOperationsHandlerService, configuration);

        // test operation
        testOperation = new Operation();
        HashMap<String, Object> testOperationFragment = new HashMap<>();
        testOperationFragment.put("givenStatus", "GIVEN_SUCCESSFUL");
        testOperation.setProperty("c8y_TestOperation", testOperationFragment);
        
        // conf operation
        confOperation = new Operation();
        HashMap<String, Object> confOperationFragment = new HashMap<>();
        confOperationFragment.put("config", "agent.yaml");
        confOperation.setProperty("c8y_Configuration", confOperationFragment);
        
        // software operation
        softwareOperation = new Operation();
        softwareOperation.setProperty("c8y_SoftwareList", "[]");
        
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
