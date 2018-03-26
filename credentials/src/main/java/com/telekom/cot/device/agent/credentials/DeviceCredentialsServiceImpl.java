package com.telekom.cot.device.agent.credentials;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.*;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.CredentialsServiceException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.service.configuration.AgentCredentials;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.HardwareProperties;

public class DeviceCredentialsServiceImpl extends AbstractAgentService implements DeviceCredentialsService {

    private static final int HTTP_NOT_FOUND = 404;
	
    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceCredentialsServiceImpl.class);

    private PlatformService platformService;
    private DeviceCredentialsServiceConfiguration configuration;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws AbstractAgentException {
        platformService = getService(PlatformService.class);
        configuration = getConfigurationManager().getConfiguration(DeviceCredentialsServiceConfiguration.class);

        super.start();
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public String getDeviceId() throws AbstractAgentException {
	    DeviceCredentialsServiceConfiguration.DeviceIdTemplates template = configuration.getDeviceIdTemplate();
	    
	    if (template == DeviceCredentialsServiceConfiguration.DeviceIdTemplates.EXTERNAL_ID_VALUE) {
	        return platformService.getExternalIdValue();
	    }
	    
	    if (template == DeviceCredentialsServiceConfiguration.DeviceIdTemplates.HARDWARE_SERIAL) {
			return getService(SystemService.class).getProperties(HardwareProperties.class).getSerialNumber();
	    }
				
		LOGGER.error("not supported device id template");
		throw new AbstractAgentException("not supported device id template") {
		    private static final long serialVersionUID = 1L;
		};
	}
    
    /**
     * {@inheritDoc}
     */
	@Override
	public boolean credentialsAvailable() {
		try {
			// try to get local credentials 
			getAgentCredentialsManager().getCredentials();
			LOGGER.info("found local device credentials");
			return true;
		} catch (AbstractAgentException e) {
			// no local credentials found
			LOGGER.info("found no local device credentials", e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AgentCredentials requestCredentials() throws AbstractAgentException {
		// stop platform service, set bootstrap credentials (don't persist) and start platform service
		platformService.stop();
		getAgentCredentialsManager().setCredentials(configuration.getBootstrapCredentials());
		platformService.start();
		
		// get credentials from platform and stop platform service
		AgentCredentials credentials = getDeviceCredentials();
		platformService.stop();
		return credentials;
	}

    /**
     * Get the credentials from the CoT.
     * 
     * @return
     * @throws CredentialsServiceException
     */
    private AgentCredentials getDeviceCredentials() throws AbstractAgentException {
        do {
            // try to get device credentials
            try {
                LOGGER.debug("try to get device credentials");
                return platformService.getDeviceCredentials(getDeviceId());
            } catch (PlatformServiceException platformServiceException) {
                int httpStatus = platformServiceException.getHttpStatus();
                if (httpStatus != HTTP_NOT_FOUND) {
                    throw createExceptionAndLog(CredentialsServiceException.class, LOGGER, "HTTP status 404 was expected", platformServiceException);
                }
            }

            try {
                TimeUnit.SECONDS.sleep(configuration.getInterval());
            } catch (InterruptedException e) {
                throw createExceptionAndLog(CredentialsServiceException.class, LOGGER, "interrupted exception", e);
            }

        } while (true);
    }
}
