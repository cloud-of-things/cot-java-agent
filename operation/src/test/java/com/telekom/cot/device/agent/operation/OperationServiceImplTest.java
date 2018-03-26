package com.telekom.cot.device.agent.operation;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.OperationServiceException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.operation.TestOperationHandlerService;
import com.telekom.cot.device.agent.operation.TestOperationHandlerService.ExceptionLocation;
import com.telekom.cot.device.agent.operation.handler.OperationsHandlerService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.SoftwareProperties;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationCollection;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;
import com.telekom.m2m.cot.restsdk.library.devicemanagement.SupportedOperations;

public class OperationServiceImplTest {

    @Mock
    private Logger mockLogger;
    @Mock
    private AgentServiceProvider mockServiceProvider;
    @Mock
    private ConfigurationManager mockConfigurationManager;
    @Mock
    private PlatformService mockPlatformService;
    @Mock
    private InventoryService mockInventoryService;
    @Mock
    private OperationCollection mockOperationCollection;
    @Mock
    private OperationCollection mockOperationCollectionRestart;
    @Mock
    private OperationCollection mockOperationCollectionSoftwareList;
    @Mock
    private SystemService mockSystemService;

    // service
    private OperationServiceImpl operationService = new OperationServiceImpl();
    // handlers
    private TestOperationHandlerService handlerServiceStopExc = //
            new TestOperationHandlerService(new RuntimeException(), ExceptionLocation.STOP, OperationStatus.SUCCESSFUL);

    private TestOperationHandlerService handlerServiceStartExc = //
            new TestOperationHandlerService(new RuntimeException(), ExceptionLocation.START);

    private TestOperationHandlerService handlerServiceSuccessful = //
            new TestOperationHandlerService(OperationStatus.SUCCESSFUL);
    private TestOperationHandlerService handlerServiceFailed = //
            new TestOperationHandlerService(OperationStatus.FAILED);

    private TestOperationHandlerService handlerServiceExecuteRunExc = //
            new TestOperationHandlerService(new RuntimeException(), ExceptionLocation.EXECUTE);
    private TestOperationHandlerService handlerServiceExecuteAgentExc = //
            new TestOperationHandlerService(new OperationServiceException("test"), ExceptionLocation.EXECUTE);

    private OperationServiceConfiguration operationServiceConfig = new OperationServiceConfiguration();
    private Operation operation = new Operation();
    private Operation operationRestart = new Operation();
    private Operation operationSoftwareList = new Operation();
    private SoftwareProperties softwareProperties; 

    @Before
    public void setUp() throws AbstractAgentException {

        operation.set("c8y_Test", "");

        // Configuration
        operationServiceConfig = new OperationServiceConfiguration();
        operationServiceConfig.setInterval(10);
        operationServiceConfig.setResultSize(1);
        operationServiceConfig.setShutdownTimeout(1000);
        operationServiceConfig.setHandlersShutdownTimeout(500);

        // initialize mocks
        MockitoAnnotations.initMocks(this);
        InjectionUtil.injectStatic(operationService.getClass(), mockLogger);
        InjectionUtil.inject(operationService, mockServiceProvider);
        InjectionUtil.inject(operationService, mockConfigurationManager);
        
        // softwareProperties
        softwareProperties = new SoftwareProperties();
        softwareProperties.addSoftware("device-agent-raspbian", "0.8.0", "url");
        
        // operationSoftwareList
        operationSoftwareList = createOperationSoftwareList();
        

        // behavior
        when(mockServiceProvider.getService(PlatformService.class)).thenReturn(mockPlatformService);
        when(mockServiceProvider.getService(InventoryService.class)).thenReturn(mockInventoryService);
        when(mockServiceProvider.getService(SystemService.class)).thenReturn(mockSystemService);
        when(mockConfigurationManager.getConfiguration(OperationServiceConfiguration.class))
                .thenReturn(operationServiceConfig);
        // operation
        when(mockPlatformService.getOperationCollection(OperationStatus.PENDING,
                operationServiceConfig.getResultSize())).thenReturn(mockOperationCollection);
        when(mockOperationCollection.getOperations()).thenReturn(new Operation[] { operation });
        when(mockOperationCollection.hasNext()).thenReturn(false);
        // c8y_Restart
        when(mockPlatformService.getOperationCollection("c8y_Restart", OperationStatus.EXECUTING, 10))
                .thenReturn(mockOperationCollectionRestart);
        when(mockOperationCollectionRestart.getOperations()).thenReturn(new Operation[] { operationRestart });
        when(mockOperationCollectionRestart.hasNext()).thenReturn(false);
        // c8y_SoftwareList
        when(mockPlatformService.getOperationCollection("c8y_SoftwareList", OperationStatus.EXECUTING, 10))
                .thenReturn(mockOperationCollectionSoftwareList);
        when(mockOperationCollectionSoftwareList.getOperations()).thenReturn(new Operation[] { operationSoftwareList });
        when(mockOperationCollectionSoftwareList.hasNext()).thenReturn(false);        
        when(mockSystemService.getProperties(SoftwareProperties.class)).thenReturn(softwareProperties);
    }

    @After
    public void tearDown() {
        assertEquals(OperationStatus.SUCCESSFUL, operationRestart.getStatus());
        assertEquals(OperationStatus.SUCCESSFUL, operationSoftwareList.getStatus());
    }

    @Test
    public void testOperationServiceImplStartExc() throws Exception {

        List<OperationsHandlerService> handlers = new ArrayList<>();
        handlers.add(handlerServiceStartExc);

        when(mockServiceProvider.getServices(OperationsHandlerService.class)).thenReturn(handlers);

        /**
         * Test
         */

        operationService.start();
        Thread.sleep(10);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */

        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertTrue(handlerServiceStartExc.isStarted());
        assertFalse(handlerServiceStartExc.isStoped());
        assertEquals(OperationStatus.FAILED, operation.getStatus());
        assertEquals(0, getOperationsHandlerServices(operationService).size());

        /**
         * verify (but only one update)
         */

        verify(mockInventoryService).update(any(SupportedOperations.class));
    }

    @Test
    public void testOperationServiceImplStopExc() throws Exception {

        List<OperationsHandlerService> handlers = new ArrayList<>();
        handlers.add(handlerServiceStopExc);

        when(mockServiceProvider.getServices(OperationsHandlerService.class)).thenReturn(handlers);

        /**
         * Test
         */

        operationService.start();
        Thread.sleep(10);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */
        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertTrue(handlerServiceStopExc.isStarted());
        assertTrue(handlerServiceStopExc.isStoped());
        assertEquals(operation, handlerServiceStopExc.getOperation());
        assertEquals(OperationStatus.SUCCESSFUL, handlerServiceStopExc.getOperation().getStatus());

        /**
         * verify (but only one update)
         */

        verify(mockInventoryService).update(any(SupportedOperations.class));
        verify(mockPlatformService, atLeast(2)).updateOperation(operation);
    }

    @Test(expected=OperationServiceException.class)
    public void testOperationServiceImplTwoOperationsWithSameName() throws Exception {

        List<OperationsHandlerService> handlers = new ArrayList<>();
        handlers.add(handlerServiceSuccessful);
        handlers.add(handlerServiceSuccessful);

        when(mockServiceProvider.getServices(OperationsHandlerService.class)).thenReturn(handlers);

        /**
         * Test
         */

        operationService.start();
    }

    @Test
    public void testOperationServiceImplSuccessful()
            throws AbstractAgentException, InterruptedException, IllegalArgumentException, IllegalAccessException {

        List<OperationsHandlerService> handlers = new ArrayList<>();
        handlers.add(handlerServiceSuccessful);

        when(mockServiceProvider.getServices(OperationsHandlerService.class)).thenReturn(handlers);

        /**
         * Test
         */

        operationService.start();
        Thread.sleep(10);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */

        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertTrue(handlerServiceSuccessful.isStarted());
        assertTrue(handlerServiceSuccessful.isStoped());
        assertEquals(operation, handlerServiceSuccessful.getOperation());
        assertEquals(OperationStatus.SUCCESSFUL, handlerServiceSuccessful.getOperation().getStatus());

        /**
         * verify (but only one update)
         */

        verify(mockInventoryService).update(any(SupportedOperations.class));
        verify(mockPlatformService, atLeast(2)).updateOperation(operation);

    }

    @Test
    public void testOperationServiceImplFailed()
            throws AbstractAgentException, InterruptedException, IllegalArgumentException, IllegalAccessException {

        List<OperationsHandlerService> handlers = new ArrayList<>();
        handlers.add(handlerServiceFailed);

        when(mockServiceProvider.getServices(OperationsHandlerService.class)).thenReturn(handlers);

        /**
         * Test
         */

        operationService.start();
        Thread.sleep(10);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */
        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertTrue(handlerServiceFailed.isStarted());
        assertTrue(handlerServiceFailed.isStoped());
        assertEquals(operation, handlerServiceFailed.getOperation());
        assertEquals(OperationStatus.FAILED, handlerServiceFailed.getOperation().getStatus());

        /**
         * verify (but only one update)
         */

        verify(mockInventoryService).update(any(SupportedOperations.class));
        verify(mockPlatformService, atLeast(2)).updateOperation(operation);

    }

    @Test
    public void testOperationServiceImplExecuteWithRuntimeException()
            throws AbstractAgentException, InterruptedException, IllegalArgumentException, IllegalAccessException {

        List<OperationsHandlerService> handlers = new ArrayList<>();
        handlers.add(handlerServiceExecuteRunExc);

        when(mockServiceProvider.getServices(OperationsHandlerService.class)).thenReturn(handlers);

        /**
         * Test
         */

        operationService.start();
        Thread.sleep(10);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */
        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertTrue(handlerServiceExecuteRunExc.isStarted());
        assertTrue(handlerServiceExecuteRunExc.isStoped());
        assertEquals(operation, handlerServiceExecuteRunExc.getOperation());
        assertEquals(OperationStatus.FAILED, handlerServiceExecuteRunExc.getOperation().getStatus());

        /**
         * verify (but only one update)
         */
        verify(mockInventoryService).update(any(SupportedOperations.class));
        verify(mockPlatformService, atLeast(2)).updateOperation(operation);
    }

    @Test
    public void testOperationServiceImplExecuteWithAgentException()
            throws AbstractAgentException, InterruptedException, IllegalArgumentException, IllegalAccessException {

        List<OperationsHandlerService> handlers = new ArrayList<>();
        handlers.add(handlerServiceExecuteAgentExc);

        when(mockServiceProvider.getServices(OperationsHandlerService.class)).thenReturn(handlers);

        /**
         * Test
         */

        operationService.start();
        Thread.sleep(10);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */
        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertTrue(handlerServiceExecuteAgentExc.isStarted());
        assertTrue(handlerServiceExecuteAgentExc.isStoped());
        assertEquals(operation, handlerServiceExecuteAgentExc.getOperation());
        assertEquals(OperationStatus.FAILED, handlerServiceExecuteAgentExc.getOperation().getStatus());

        /**
         * verify (but only one update)
         */

        verify(mockInventoryService).update(any(SupportedOperations.class));
        verify(mockPlatformService, atLeast(2)).updateOperation(operation);
    }

    /**
     * Get the worker.
     * 
     * @param operationService
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private OperationWorker getOperationWorker(OperationServiceImpl operationService)
            throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = OperationServiceImpl.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("operationWorker")) {
                field.setAccessible(true);
                return (OperationWorker) field.get(operationService);
            }
        }
        return null;
    }

    /**
     * 
     * @param operationService
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    private List<OperationsHandlerService> getOperationsHandlerServices(OperationServiceImpl operationService)
            throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = OperationServiceImpl.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("operationHandlerServices")) {
                field.setAccessible(true);
                return (List<OperationsHandlerService>) field.get(operationService);
            }
        }
        return null;
    }

    private Operation createOperationSoftwareList() {
        Operation operation = new Operation();
        JsonArray softwareListArray =new JsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "device-agent-raspbian");
        jsonObject.addProperty("version", "0.8.0");
        jsonObject.addProperty("url", "https://asterix.ram.m2m.telekom.com/inventory/binaries/5380090");
        softwareListArray.add(jsonObject);
        operation.set("c8y_SoftwareList", softwareListArray);
        return operation;
    }
}
