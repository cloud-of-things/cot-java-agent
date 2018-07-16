package com.telekom.cot.device.agent.app;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.service.AgentServiceManager;
import com.telekom.cot.device.agent.system.SystemService;

public class AppBootstrapServiceManagerImplTest {

    @Mock
    private ConfigurationManager mockConfigurationManager;
    @Mock
    private AgentCredentialsManager mockAgentCredentialsManager;
    @Mock
    private AgentServiceManager mockAgentServiceManager;
    @Mock
    private SystemService mockSystemService;
    @Mock
    private Runtime mockRuntime;
    //
    private AppBootstrapServiceManager bootstrapServiceManager;
    private Path agentFilePath;
    private Path tempFilePath;

    @Before
    public void setUp() throws Exception {
        String workingDir = System.getProperty("user.dir");
        tempFilePath = Files.createTempDirectory(Paths.get(workingDir, "target"), "test");
        agentFilePath = Files.createFile(Paths.get(tempFilePath.toString(), "agent.yaml"));
        // init and inject mocks
        MockitoAnnotations.initMocks(this);
        // create factory
        bootstrapServiceManager = new AppBootstrapFactory.AppBootstrapServiceManagerImpl(mockAgentServiceManager,
                        mockAgentCredentialsManager, mockConfigurationManager);
        InjectionUtil.inject(bootstrapServiceManager, mockRuntime);
        // behavior of mocked AgentServiceManager
        when(mockAgentServiceManager.getService(SystemService.class)).thenReturn(mockSystemService);
    }

    @After
    public void tearDown() {
        try {
            Files.delete(agentFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Files.delete(tempFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * get
     */
    @Test
    public void testGetService() throws AppMainException {
        assertThat(bootstrapServiceManager.getService(SystemService.class), Matchers.notNullValue(SystemService.class));
    }

    @Test
    public void testGetServiceExc() throws AppMainException, AbstractAgentException {
        doThrow(new TestAgentException()).when(mockAgentServiceManager).getService(SystemService.class);
        try {
            bootstrapServiceManager.getService(SystemService.class);
            fail();
        } catch (AppMainException exc) {
            // ignore
        }
    }

    /*
     * stop
     */
    @Test
    public void testStopService() throws AppMainException, AbstractAgentException {
        when(mockSystemService.isStarted()).thenReturn(true);
        bootstrapServiceManager.stopService(SystemService.class);
        verify(mockSystemService).stop();
    }

    @Test
    public void testStopServiceExc() throws AppMainException, AbstractAgentException {
        doThrow(new TestAgentException()).when(mockAgentServiceManager).getService(SystemService.class);
        try {
            bootstrapServiceManager.stopService(SystemService.class);
            fail();
        } catch (AppMainException exc) {
            // ignore
        }
    }

    /*
     * start
     */
    @Test
    public void testStartService() throws AppMainException, AbstractAgentException {
        when(mockSystemService.isStarted()).thenReturn(false);
        bootstrapServiceManager.startService(SystemService.class);
        verify(mockSystemService).start();
    }

    @Test
    public void testStartServiceExc() throws AppMainException, AbstractAgentException {
        doThrow(new TestAgentException()).when(mockAgentServiceManager).getService(SystemService.class);
        try {
            bootstrapServiceManager.startService(SystemService.class);
            fail();
        } catch (AppMainException exc) {
            // ignore
        }
    }

    /*
     * loadAndInitializeAgentServices
     */
    @Test
    public void testLoadAndInitializeAgentServices() throws AppMainException, AbstractAgentException {
        bootstrapServiceManager.loadAndInitializeAgentServices();
        verify(mockAgentServiceManager).loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
        verify(mockRuntime).addShutdownHook(Mockito.any(AppShutdown.class));
    }

    @Test
    public void testLoadAndInitializeAgentServicesExc() throws AppMainException, AbstractAgentException {
        doThrow(new TestAgentException()).when(mockAgentServiceManager)
                        .loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
        try {
            bootstrapServiceManager.loadAndInitializeAgentServices();
            fail();
        } catch (AppMainException exc) {
            // ignore
        }
    }

    static class TestAgentException extends AbstractAgentException {

        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        public TestAgentException() {
            super("test");
        }
    }
}
