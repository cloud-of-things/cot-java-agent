package com.telekom.cot.device.agent.operation.softwareupdate;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.assertNotEmpty;
import static com.telekom.cot.device.agent.common.util.AssertionUtil.createExceptionAndLog;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.OperationServiceException;
import com.telekom.cot.device.agent.operation.handler.AbstractOperationExecute;
import com.telekom.cot.device.agent.operation.handler.OperationUtil;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;
import com.telekom.cot.device.agent.system.properties.Software;

/**
 * Example: ExtensibleObject{ anyObject={ creationTime=Tue Mar 06 12:28:14 CET 2018, c8y_SoftwareList=[
 * SoftwareListEntry [name=name, version=version, url=url] ],
 * self=https://asterix.ram.m2m.telekom.com/devicecontrol/operations/6433019, description=SoftwareList device,
 * id=6433019, deviceId=6433011, status=PENDING}}
 *
 */
public class SoftwareUpdateExecute extends AbstractOperationExecute<SoftwareUpdateConfig> {

    private static final String CANT_PERFORM_EXECUTE = "can't perform SoftwareList execute: ";
    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SoftwareUpdateExecute.class);

    @Override
    public OperationStatus perform() throws AbstractAgentException {
        Software software = OperationUtil.getSoftwareToUpdate(getOperation());

        assertNotEmpty(software.getVersion(), OperationServiceException.class, LOGGER, CANT_PERFORM_EXECUTE + "version is missed");

        // get download url
        URL downloadUrl;
        try {
            downloadUrl = new URL(software.getUrl());
        } catch (Exception e) {
            throw createExceptionAndLog(OperationServiceException.class, LOGGER, CANT_PERFORM_EXECUTE + "can't get download url");
        }
        
        if(isCallback()) {
            getCallback(URL.class).finished(downloadUrl);
            return OperationStatus.EXECUTING;
        }

        return OperationStatus.FAILED;
    }
}
