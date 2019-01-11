package com.telekom.cot.device.agent.operation.handler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.telekom.cot.device.agent.common.ChecksumAlgorithm;
import com.telekom.cot.device.agent.common.exc.OperationHandlerServiceException;
import com.telekom.cot.device.agent.common.exc.SoftwareInstallerException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.operation.operations.SoftwareUpdateOperation;
import com.telekom.cot.device.agent.operation.softwareupdate.SoftwareInstaller;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;

public class SoftwareUpdateOperationHandlerTest {

    @Mock
    private SoftwareInstaller mockSoftwareInstaller;
    @Mock
    private PlatformService mockPlatformService;
    @Mock
    private SoftwareUpdateConfiguration mockConfiguration;

    private SoftwareUpdateOperationHandler handler;
    private SoftwareUpdateOperation operation;
    private JsonObject softwareJsonObject = new JsonObject();
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        handler = new SoftwareUpdateOperationHandler();

        when(mockConfiguration.getChecksumAlgorithm()).thenReturn(ChecksumAlgorithm.MD5);
        
        softwareJsonObject = new JsonObject();
        softwareJsonObject.addProperty("name", "test-agent");
        softwareJsonObject.addProperty("version", "1.2.3");
        softwareJsonObject.addProperty("url", "https://1.2.3.4/download");
        
        JsonArray softwareJsonArray = new JsonArray();
        softwareJsonArray.add(softwareJsonObject);

        operation = new SoftwareUpdateOperation();
        operation.setProperty(operation.getOperationName(), softwareJsonArray);
        
        InjectionUtil.inject(handler, mockSoftwareInstaller);
        InjectionUtil.inject(handler, mockPlatformService);
        InjectionUtil.inject(handler, mockConfiguration);
    }
    
    @Test
    public void testStart() throws Exception {
        handler.start();
    }
    
    @Test
    public void testGetSupportedOperationType() {
        assertSame(SoftwareUpdateOperation.class, handler.getSupportedOperationType());
    }

    @Test(expected=OperationHandlerServiceException.class)
    public void testExecuteNullOperation() throws Exception {
        handler.execute(null);
    }

    @Test(expected=OperationHandlerServiceException.class)
    public void testExecuteNoSoftwareList() throws Exception {
        operation.removeProperty(operation.getOperationName());
        handler.execute(operation);
    }

    @Test(expected=OperationHandlerServiceException.class)
    public void testExecuteNoSoftwareVersion() throws Exception {
        softwareJsonObject.remove("version");
        softwareJsonObject.addProperty("version", "");
        handler.execute(operation);
    }

    @Test(expected=OperationHandlerServiceException.class)
    public void testExecuteNoSoftwareUrl() throws Exception {
        softwareJsonObject.remove("url");
        softwareJsonObject.addProperty("url", "");
        handler.execute(operation);
    }

    @Test(expected=SoftwareInstallerException.class)
    public void testExecuteSoftwareInstallerDownloadException() throws Exception {
        doThrow(new SoftwareInstallerException("test")).when(mockSoftwareInstaller).downloadSoftware(eq(mockPlatformService),
                        any(URL.class),  eq(ChecksumAlgorithm.MD5));
        handler.execute(operation);
    }

    @Test(expected=SoftwareInstallerException.class)
    public void testExecuteSoftwareInstallerInstallException() throws Exception {
        doThrow(new SoftwareInstallerException("test")).when(mockSoftwareInstaller).installSoftware();
        handler.execute(operation);
    }

    @Test
    public void testExecute() throws Exception {
        assertEquals(OperationStatus.EXECUTING, handler.execute(operation));
        
        verify(mockConfiguration, times(1)).getChecksumAlgorithm();
        verify(mockSoftwareInstaller, times(1)).downloadSoftware(eq(mockPlatformService), any(URL.class), eq(ChecksumAlgorithm.MD5));
        verify(mockSoftwareInstaller, times(1)).installSoftware();
    }
}
