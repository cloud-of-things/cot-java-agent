package com.telekom.cot.device.agent.operation.handler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.ConfigurationUpdateException;
import com.telekom.cot.device.agent.common.exc.InventoryServiceException;
import com.telekom.cot.device.agent.common.exc.OperationHandlerServiceException;
import com.telekom.cot.device.agent.common.exc.PropertyNotFoundException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.operation.operations.ConfigurationUpdateOperation;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.ConfigurationProperties;


public class ConfigurationUpdateOperationHandlerTest {

    private static final String CONFIGURATION = "test configuration string";
    
    @Mock
    private ConfigurationManager mockConfigurationManager;
    @Mock
    private SystemService mockSystemService;
    @Mock
    private InventoryService mockInventoryService;
    @Mock
    private ConfigurationProperties mockConfigurationProperties;

    private ConfigurationUpdateOperationHandler handler;
    private ConfigurationUpdateOperation operation;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        handler = new ConfigurationUpdateOperationHandler();
        
        InjectionUtil.inject(handler, mockConfigurationManager);
        InjectionUtil.inject(handler, mockSystemService);
        InjectionUtil.inject(handler, mockInventoryService);
        
        when(mockSystemService.getProperties(ConfigurationProperties.class)).thenReturn(mockConfigurationProperties);
        
        operation = new ConfigurationUpdateOperation();
        operation.setConfiguration(CONFIGURATION);
    }
    
    @Test
    public void testGetSupportedOperationType() {
        assertSame(ConfigurationUpdateOperation.class, handler.getSupportedOperationType());
    }

    @Test(expected=OperationHandlerServiceException.class)
    public void testExecuteNullOperation() throws Exception {
        handler.execute(null);
    }

    @Test(expected=OperationHandlerServiceException.class)
    public void testExecuteOperationNoConfiguration() throws Exception {
        operation.removeProperty(operation.getOperationName());
        handler.execute(operation);
    }

    @Test(expected=ConfigurationUpdateException.class)
    public void testExecuteConfigurationManagerException() throws Exception {
        doThrow(new ConfigurationUpdateException("test")).when(mockConfigurationManager).updateConfiguration(CONFIGURATION);
        handler.execute(operation);
    }

    @Test(expected=PropertyNotFoundException.class)
    public void testExecuteSystemServiceException() throws Exception {
        doThrow(new PropertyNotFoundException("test")).when(mockSystemService).getProperties(ConfigurationProperties.class);
        handler.execute(operation);
    }

    @Test(expected=InventoryServiceException.class)
    public void testExecuteInventoryServiceException() throws Exception {
        doThrow(new InventoryServiceException("test")).when(mockInventoryService).updateDevice();
        handler.execute(operation);
    }

    @Test
    public void testExecuteNullConfigurationProperties() throws Exception {
        doReturn(null).when(mockSystemService).getProperties(ConfigurationProperties.class);

        assertEquals(OperationStatus.SUCCESSFUL, handler.execute(operation));
        
        verify(mockConfigurationManager).updateConfiguration(CONFIGURATION);
        verify(mockSystemService).getProperties(ConfigurationProperties.class);
        verify(mockInventoryService).updateDevice();
    }

    @Test
    public void testExecute() throws Exception {
        assertEquals(OperationStatus.SUCCESSFUL, handler.execute(operation));
        
        verify(mockConfigurationManager).updateConfiguration(CONFIGURATION);
        verify(mockSystemService).getProperties(ConfigurationProperties.class);
        verify(mockConfigurationProperties).setConfig(CONFIGURATION);
        verify(mockInventoryService).updateDevice();
    }
}
