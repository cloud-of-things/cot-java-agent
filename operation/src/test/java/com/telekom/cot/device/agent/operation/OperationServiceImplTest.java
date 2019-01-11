package com.telekom.cot.device.agent.operation;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.common.exc.OperationServiceException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.operation.handler.OperationHandlerService;
import com.telekom.cot.device.agent.operation.operations.RestartOperation;
import com.telekom.cot.device.agent.operation.operations.SoftwareUpdateOperation;
import com.telekom.cot.device.agent.operation.operations.TestOperation;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.operation.Operation;
import com.telekom.cot.device.agent.platform.objects.operation.OperationFactory;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.SoftwareProperties;

public class OperationServiceImplTest {

    private static final String PENDING_OPERATION_ID = "123";
    
    @Mock
    private Logger mockLogger;
    @Mock
    private AgentServiceProvider mockServiceProvider;
    @Mock
    private PlatformService mockPlatformService;
    @Mock
    private InventoryService mockInventoryService;
    @Mock
    private SystemService mockSystemService;
    @SuppressWarnings("rawtypes")
    @Mock
    private OperationHandlerService mockOperationHandlerService;

    private boolean requestedNextPendingOperationAlready;
    
    // service
    private OperationServiceImpl operationService = new OperationServiceImpl();
    
    private OperationServiceConfiguration operationServiceConfig = new OperationServiceConfiguration();
    
    // operation objects
    private Operation operationPending = new Operation(PENDING_OPERATION_ID) {};
    private RestartOperation operationRestart = new RestartOperation();
    private SoftwareUpdateOperation operationSoftware = new SoftwareUpdateOperation();
    private SoftwareProperties softwareProperties; 

    // operation list objects
    private List<Operation> operationPendingList = new ArrayList<Operation>();
    private List<SoftwareUpdateOperation> operationSoftwareList = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws AbstractAgentException {

        // set up operationPendingList
        operationPending.setProperty(OperationFactory.getOperationName(TestOperation.class), "");
        operationPendingList.add(operationPending);
        
        // set up operationSoftwareList
        operationSoftware = createOperationSoftwareList();
        operationSoftwareList.add(operationSoftware);

        // Configuration
        operationServiceConfig = new OperationServiceConfiguration();
        operationServiceConfig.setInterval(1);
        operationServiceConfig.setShutdownTimeout(1000);
        operationServiceConfig.setHandlersShutdownTimeout(500);

        // initialize mocks
        MockitoAnnotations.initMocks(this);
        InjectionUtil.injectStatic(operationService.getClass(), mockLogger);
        InjectionUtil.inject(operationService, mockServiceProvider);
        InjectionUtil.inject(operationService, mockPlatformService);
        InjectionUtil.inject(operationService, mockInventoryService);
        InjectionUtil.inject(operationService, mockSystemService);
        InjectionUtil.inject(operationService, operationServiceConfig);
        
        // softwareProperties
        softwareProperties = new SoftwareProperties();
        softwareProperties.addSoftware("device-agent-raspbian", "0.8.0", "url");

        when(mockServiceProvider.getServices(OperationHandlerService.class)).thenReturn(Arrays.asList(mockOperationHandlerService));
        
        when(mockOperationHandlerService.getSupportedOperationType()).thenReturn(TestOperation.class);
        when(mockOperationHandlerService.execute(any(TestOperation.class))).thenReturn(OperationStatus.SUCCESSFUL);
        
        // next pending operation
        requestedNextPendingOperationAlready = false;
        doAnswer(new Answer<Operation>() {
            @Override
            public Operation answer(InvocationOnMock invocation) throws Throwable {
                // return 'operationPending' only at first time
                if (requestedNextPendingOperationAlready) {
                    return null;
                }
                
                requestedNextPendingOperationAlready = true;
                return operationPending;
            }
        }).when(mockPlatformService).getNextPendingOperation();

        // c8y_Restart
        when(mockPlatformService.getOperations(RestartOperation.class, OperationStatus.EXECUTING))
        		.thenReturn(Arrays.asList(operationRestart));

        // c8y_SoftwareList
        when(mockPlatformService.getOperations(SoftwareUpdateOperation.class, OperationStatus.EXECUTING))
        		.thenReturn(Arrays.asList(operationSoftware));
        
        when(mockSystemService.getProperties(SoftwareProperties.class)).thenReturn(softwareProperties);
    }

    @After
    public void tearDown() throws Exception {
        verify(mockPlatformService, atLeastOnce()).updateOperationStatus(operationRestart.getId(), OperationStatus.SUCCESSFUL);
//        verify(mockPlatformService, atLeastOnce()).updateOperationStatus(operationSoftware.getId(), OperationStatus.SUCCESSFUL);
    }

    /**
     * Throw exception while starting OperationHandlerService
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testOperationServiceImplStartException() throws Exception {
        @SuppressWarnings("serial")
        Exception exception = new AbstractAgentException("test") {};
        doThrow(exception).when(mockOperationHandlerService).start();
        
        operationService.start();
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertEquals(0, getOperationsHandlerServices(operationService).size());
        assertEquals(OperationStatus.FAILED, operationPending.getStatus());

        verify(mockOperationHandlerService, times(1)).getSupportedOperationType();
        verify(mockOperationHandlerService, never()).execute(any(TestOperation.class));
        verify(mockLogger).error("can't start the operation handler " + mockOperationHandlerService.getClass(), exception);
        verify(mockPlatformService).updateSupportedOperations(new ArrayList<>());
    }

    
    /**
     * Throw exception while stopping OperationHandlerService
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testOperationServiceImplStopException() throws Exception {
        @SuppressWarnings("serial")
        Exception exception = new AbstractAgentException("test") {};
        doThrow(exception).when(mockOperationHandlerService).stop();

        operationService.start();
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertEquals(1, getOperationsHandlerServices(operationService).size());
        assertSame(mockOperationHandlerService, getOperationsHandlerServices(operationService).get(0));

        verify(mockOperationHandlerService, times(3)).getSupportedOperationType();
        verify(mockOperationHandlerService, times(1)).execute(any(TestOperation.class));
        verify(mockPlatformService).updateSupportedOperations(Arrays.asList("c8y_TestOperation"));
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.SUCCESSFUL);
    }

    /**
     * Test 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(expected=OperationServiceException.class)
    public void testOperationServiceImplTwoOperationsWithSameName() throws Exception {
        doReturn(Arrays.asList(mockOperationHandlerService, mockOperationHandlerService)).when(mockServiceProvider).getServices(OperationHandlerService.class);

        operationService.start();

        verify(mockOperationHandlerService, times(1)).getSupportedOperationType();
        verify(mockOperationHandlerService, never()).execute(any(TestOperation.class));
        verify(mockPlatformService, never()).updateSupportedOperations(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOperationServiceImplSuccessful() throws Exception {
        operationService.start();
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertEquals(1, getOperationsHandlerServices(operationService).size());
        assertSame(mockOperationHandlerService, getOperationsHandlerServices(operationService).get(0));

        verify(mockOperationHandlerService, times(3)).getSupportedOperationType();
        verify(mockOperationHandlerService, times(1)).execute(any(TestOperation.class));
        verify(mockPlatformService).updateSupportedOperations(Arrays.asList("c8y_TestOperation"));
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.SUCCESSFUL);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOperationServiceImplFailed() throws Exception {
        doReturn(OperationStatus.FAILED).when(mockOperationHandlerService).execute(any(TestOperation.class));
        
        operationService.start();
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertEquals(1, getOperationsHandlerServices(operationService).size());
        assertSame(mockOperationHandlerService, getOperationsHandlerServices(operationService).get(0));

        verify(mockOperationHandlerService, times(3)).getSupportedOperationType();
        verify(mockOperationHandlerService, times(1)).execute(any(TestOperation.class));
        verify(mockPlatformService).updateSupportedOperations(Arrays.asList("c8y_TestOperation"));
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.FAILED);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOperationServiceImplExecuteWithRuntimeException() throws Exception {
        when(mockOperationHandlerService.execute(any(TestOperation.class))).thenThrow(new RuntimeException());

        operationService.start();
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertEquals(1, getOperationsHandlerServices(operationService).size());
        assertSame(mockOperationHandlerService, getOperationsHandlerServices(operationService).get(0));

        verify(mockOperationHandlerService, times(3)).getSupportedOperationType();
        verify(mockOperationHandlerService, times(1)).execute(any(TestOperation.class));
        verify(mockPlatformService).updateSupportedOperations(Arrays.asList("c8y_TestOperation"));
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.FAILED);
    }

    @SuppressWarnings({ "unchecked", "serial" })
    @Test
    public void testOperationServiceImplExecuteWithAgentException() throws Exception {
        when(mockOperationHandlerService.execute(any(TestOperation.class))).thenThrow(new AbstractAgentException("test") {});

        operationService.start();
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertEquals(1, getOperationsHandlerServices(operationService).size());
        assertSame(mockOperationHandlerService, getOperationsHandlerServices(operationService).get(0));

        verify(mockOperationHandlerService, times(3)).getSupportedOperationType();
        verify(mockOperationHandlerService, times(1)).execute(any(TestOperation.class));
        verify(mockPlatformService).updateSupportedOperations(Arrays.asList("c8y_TestOperation"));
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.FAILED);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testOperationServiceImplExecuteWithAgentServiceNotFoundException() throws Exception {
    	doThrow(new AgentServiceNotFoundException("test")).when(mockServiceProvider).getServices(OperationHandlerService.class);

        operationService.start();
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertEquals(0, getOperationsHandlerServices(operationService).size());

        verify(mockOperationHandlerService, never()).getSupportedOperationType();
        verify(mockOperationHandlerService, never()).execute(any(TestOperation.class));
        verify(mockPlatformService).updateSupportedOperations(new ArrayList<>());
        verify(mockPlatformService, never()).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.FAILED);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testOperationServiceImplExecuteWithAgentServiceNotFoundException1() throws Exception {
        doThrow(new AgentServiceNotFoundException("test")).when(mockPlatformService).updateSupportedOperations(any());

        operationService.start();
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertEquals(0, getOperationsHandlerServices(operationService).size());

        verify(mockOperationHandlerService, times(2)).getSupportedOperationType();
        verify(mockOperationHandlerService, never()).execute(any(TestOperation.class));
        verify(mockPlatformService).updateSupportedOperations(Arrays.asList("c8y_TestOperation"));
        verify(mockPlatformService, never()).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.FAILED);
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<OperationHandlerService> getOperationsHandlerServices(OperationServiceImpl operationService)
            throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = OperationServiceImpl.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("operationHandlerServices")) {
                field.setAccessible(true);
                return (List<OperationHandlerService>) field.get(operationService);
            }
        }
        return null;
    }

    private SoftwareUpdateOperation createOperationSoftwareList() {
        SoftwareUpdateOperation operation = new SoftwareUpdateOperation();
        JsonArray softwareListArray =new JsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "device-agent-raspbian");
        jsonObject.addProperty("version", "0.8.0");
        jsonObject.addProperty("url", "https://asterix.ram.m2m.telekom.com/inventory/binaries/5380090");
        softwareListArray.add(jsonObject);
        operation.setProperty(operation.getOperationName(), softwareListArray);
        return operation;
    }
}
