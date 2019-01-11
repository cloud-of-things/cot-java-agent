package com.telekom.cot.device.agent.operation.handler;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.*;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.OperationHandlerServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.operation.operations.SoftwareUpdateOperation;
import com.telekom.cot.device.agent.operation.softwareupdate.SoftwareInstaller;
import com.telekom.cot.device.agent.operation.softwareupdate.SoftwareInstallerFactory;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.system.properties.Software;


public class SoftwareUpdateOperationHandler extends AbstractAgentService implements OperationHandlerService<SoftwareUpdateOperation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoftwareUpdateOperationHandler.class);
    
    private SoftwareInstaller softwareInstaller;

    @Inject
    private PlatformService platformService;
    
    @Inject
    private SoftwareUpdateConfiguration configuration;
    
    @Override
    public void start() throws AbstractAgentException {
        softwareInstaller = SoftwareInstallerFactory.getInstance();
        super.start();
    }
    
    @Override
    public Class<SoftwareUpdateOperation> getSupportedOperationType() {
        return SoftwareUpdateOperation.class;
    }

    @Override
    public OperationStatus execute(SoftwareUpdateOperation operation) throws AbstractAgentException {
        assertNotNull(operation, OperationHandlerServiceException.class, LOGGER, "no software update operation given");
        LOGGER.info("execute software update operation {}", operation.getProperties());
        
        URL url = getSoftwareDownloadURL(operation);
        LOGGER.info("download agent software by URL {}", url);
        softwareInstaller.downloadSoftware(platformService, url, configuration.getChecksumAlgorithm());
            
        LOGGER.info("install downloaded agent software");
        softwareInstaller.installSoftware();
        return OperationStatus.EXECUTING;
    }
    
    private URL getSoftwareDownloadURL(SoftwareUpdateOperation operation) throws AbstractAgentException {
        Software software = operation.getSoftware();
        assertNotNull(software, OperationHandlerServiceException.class, LOGGER, "no software information found @ software update operation");
        assertNotEmpty(software.getVersion(), OperationHandlerServiceException.class, LOGGER, "software version is missed");
        
        try {
            return new URL(software.getUrl());
        } catch (Exception e) {
            throw createExceptionAndLog(OperationHandlerServiceException.class, LOGGER, "can't get download url from software update operation", e);
        }
    }
}
