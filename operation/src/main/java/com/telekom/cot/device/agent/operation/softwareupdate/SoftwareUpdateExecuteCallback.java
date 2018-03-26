package com.telekom.cot.device.agent.operation.softwareupdate;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.ChecksumAlgorithm;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.operation.handler.OperationExecuteCallback;
import com.telekom.cot.device.agent.platform.PlatformService;

public class SoftwareUpdateExecuteCallback implements OperationExecuteCallback<URL> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoftwareUpdateExecuteCallback.class);

    private PlatformService platformService;
    private ChecksumAlgorithm checksumAlgorithm;
    
    public SoftwareUpdateExecuteCallback(PlatformService platformService, ChecksumAlgorithm checksumAlgorithm) {
        this.platformService = platformService;
        this.checksumAlgorithm = checksumAlgorithm; 
    }
    
    @Override
    public void finished(URL url) throws AbstractAgentException {
        SoftwareInstaller updater = SoftwareInstallerFactory.getInstance();

        LOGGER.info("download agent software by URL {}", url);
        updater.downloadSoftware(platformService, url, checksumAlgorithm);
        
        LOGGER.info("install downloaded agent software");
        updater.installSoftware();
        
    }
}
