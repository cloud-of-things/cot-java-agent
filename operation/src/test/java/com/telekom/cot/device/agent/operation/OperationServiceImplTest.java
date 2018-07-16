package com.telekom.cot.device.agent.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
import com.telekom.cot.device.agent.operation.TestOperationHandlerService.ExceptionLocation;
import com.telekom.cot.device.agent.operation.handler.OperationsHandlerService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;
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

    private boolean requestedNextPendingOperationAlready;
    
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
    
    // operation objects
    private Operation operationPending = new Operation(PENDING_OPERATION_ID);
    private Operation operationRestart = new Operation("0815");
    private Operation operationSoftware = new Operation("4711");
    private SoftwareProperties softwareProperties; 

    // operation list objects
    private List<Operation> operationPendingList = new ArrayList<Operation>();
    private List<Operation> operationRestartList = new ArrayList<Operation>();
    private List<Operation> operationSoftwareList = new ArrayList<Operation>();

    @Before
    public void setUp() throws AbstractAgentException {

        // set up operationPendingList
        operationPending.setProperty("c8y_Test", "");
        operationPendingList.add(operationPending);
        
        // set up operationRestartList
        operationRestartList.add(operationRestart);
        
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
        when(mockPlatformService.getOperations("c8y_Restart", OperationStatus.EXECUTING))
        		.thenReturn(operationRestartList);

        // c8y_SoftwareList
        when(mockPlatformService.getOperations("c8y_SoftwareList", OperationStatus.EXECUTING))
        		.thenReturn(operationSoftwareList);
        
        when(mockSystemService.getProperties(SoftwareProperties.class)).thenReturn(softwareProperties);
    }

    @After
    public void tearDown() throws Exception {
        verify(mockPlatformService, atLeastOnce()).updateOperationStatus(operationRestart.getId(), OperationStatus.SUCCESSFUL);
        verify(mockPlatformService, atLeastOnce()).updateOperationStatus(operationSoftware.getId(), OperationStatus.SUCCESSFUL);
    }

    /**
     * Throw exception while starting OperationHandlerService
     * @throws Exception
     */
    @Test
    public void testOperationServiceImplStartExc() throws Exception {

        List<OperationsHandlerService> handlers = new ArrayList<>();
        handlers.add(handlerServiceStartExc);

        when(mockServiceProvider.getServices(OperationsHandlerService.class)).thenReturn(handlers);

        /**
         * Test
         */        

        operationService.start();
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */

        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertTrue(handlerServiceStartExc.isStarted());
        assertFalse(handlerServiceStartExc.isStopped());
        assertEquals(OperationStatus.FAILED, operationPending.getStatus());
        assertEquals(0, getOperationsHandlerServices(operationService).size());

        /**
         * verify (but only one update)
         */

        verify(mockPlatformService).updateSupportedOperations(any());
    }

    
    /**
     * Throw exception while stopping OperationHandlerService
     * @throws Exception
     */
    @Test
    public void testOperationServiceImplStopExc() throws Exception {

        List<OperationsHandlerService> handlers = new ArrayList<>();
        handlers.add(handlerServiceStopExc);

        when(mockServiceProvider.getServices(OperationsHandlerService.class)).thenReturn(handlers);

        /**
         * Test
         */

        operationService.start();
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */
        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertTrue(handlerServiceStopExc.isStarted());
        assertTrue(handlerServiceStopExc.isStopped());
        assertEquals(operationPending, handlerServiceStopExc.getOperation());
        assertEquals(OperationStatus.SUCCESSFUL, handlerServiceStopExc.getOperation().getStatus());

        /**
         * verify (but only one update)
         */

        verify(mockPlatformService).updateSupportedOperations(any());
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.SUCCESSFUL);
    }

    /**
     * Test 
     * @throws Exception
     */
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
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */

        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertTrue(handlerServiceSuccessful.isStarted());
        assertTrue(handlerServiceSuccessful.isStopped());
        assertEquals(operationPending, handlerServiceSuccessful.getOperation());
        assertEquals(OperationStatus.SUCCESSFUL, handlerServiceSuccessful.getOperation().getStatus());

        /**
         * verify (but only one update)
         */

        verify(mockPlatformService).updateSupportedOperations(any());
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.SUCCESSFUL);
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
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */
        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertTrue(handlerServiceFailed.isStarted());
        assertTrue(handlerServiceFailed.isStopped());
        assertEquals(operationPending, handlerServiceFailed.getOperation());
        assertEquals(OperationStatus.FAILED, handlerServiceFailed.getOperation().getStatus());

        /**
         * verify (but only one update)
         */

        verify(mockPlatformService).updateSupportedOperations(any());
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.FAILED);
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
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */
        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertTrue(handlerServiceExecuteRunExc.isStarted());
        assertTrue(handlerServiceExecuteRunExc.isStopped());
        assertEquals(operationPending, handlerServiceExecuteRunExc.getOperation());
        assertEquals(OperationStatus.FAILED, handlerServiceExecuteRunExc.getOperation().getStatus());

        /**
         * verify (but only one update)
         */
        verify(mockPlatformService).updateSupportedOperations(any());
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.FAILED);
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
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */
        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertTrue(handlerServiceExecuteAgentExc.isStarted());
        assertTrue(handlerServiceExecuteAgentExc.isStopped());
        assertEquals(operationPending, handlerServiceExecuteAgentExc.getOperation());
        assertEquals(OperationStatus.FAILED, handlerServiceExecuteAgentExc.getOperation().getStatus());

        /**
         * verify (but only one update)
         */

        verify(mockPlatformService).updateSupportedOperations(any());
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.FAILED);
    }
    
    @Test
    public void testOperationServiceImplExecuteWithAgentServiceNotFoundException() 
    		throws AbstractAgentException, InterruptedException, IllegalArgumentException, IllegalAccessException {
        
    	doThrow(new AgentServiceNotFoundException("test")).when(mockServiceProvider).getServices(OperationsHandlerService.class);

        /**
         * Test
         */

        operationService.start();
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */
        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());

        /**
         * verify (but only one update)
         */

        verify(mockPlatformService).updateSupportedOperations(any());
        verify(mockPlatformService, times(1)).updateOperationStatus(PENDING_OPERATION_ID, OperationStatus.FAILED);
    }
    
    @Test
    public void testOperationServiceImplExecuteWithAgentServiceNotFoundException1() throws Exception {

    	/**
    	 * Arrange
    	 */
    	
        List<OperationsHandlerService> handlers = new ArrayList<>();
        handlers.add(handlerServiceSuccessful);

        when(mockServiceProvider.getServices(OperationsHandlerService.class)).thenReturn(handlers);
        doThrow(new AgentServiceNotFoundException("test")).when(mockPlatformService).updateSupportedOperations(any());

        /**
         * Test
         */

        operationService.start();
        TimeUnit.MILLISECONDS.sleep(100);
        operationService.stop();

        OperationWorker operationWorker = getOperationWorker(operationService);

        /**
         * Assertion
         */

        assertTrue(operationWorker.isStarted());
        assertTrue(operationWorker.isStopped());
        assertTrue(handlerServiceSuccessful.isStarted());
        assertTrue(handlerServiceSuccessful.isStopped());
        assertEquals(OperationStatus.FAILED, operationPending.getStatus());
        assertEquals(0, getOperationsHandlerServices(operationService).size());

        /**
         * verify (but only one update)
         */

        verify(mockPlatformService).updateSupportedOperations(any());
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
        operation.setProperty("c8y_SoftwareList", softwareListArray);
        return operation;
    }
}
