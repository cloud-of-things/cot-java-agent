package com.telekom.cot.device.agent.credentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.configuration.AgentCredentials;
import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.DeviceCredentialsServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.HardwareProperties;

public class DeviceCredentialsServiceImpl extends AbstractAgentService implements DeviceCredentialsService {
	
    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceCredentialsServiceImpl.class);

    @Inject
    private DeviceCredentialsServiceConfiguration configuration;
    @Inject
    private PlatformService platformService;
    @Inject
    private SystemService systemService;
    @Inject
    private AgentCredentialsManager credentialsManager;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws AbstractAgentException {
        AssertionUtil.assertNotNull(configuration, DeviceCredentialsServiceException.class, LOGGER, "no configuration given");
        AssertionUtil.assertNotNull(platformService, DeviceCredentialsServiceException.class, LOGGER, "no platform service given");
        AssertionUtil.assertNotNull(systemService, DeviceCredentialsServiceException.class, LOGGER, "no system service given");
        AssertionUtil.assertNotNull(credentialsManager, DeviceCredentialsServiceException.class, LOGGER, "no credentials manager given");
        super.start();
    }

	private String getDeviceId() throws AbstractAgentException {
	    DeviceCredentialsServiceConfiguration.DeviceIdTemplates template = configuration.getDeviceIdTemplate();
	    
	    if (template == DeviceCredentialsServiceConfiguration.DeviceIdTemplates.EXTERNAL_ID_VALUE) {
	        return platformService.getExternalIdValue();
	    }
	    
	    if (template == DeviceCredentialsServiceConfiguration.DeviceIdTemplates.HARDWARE_SERIAL) {
			return systemService.getProperties(HardwareProperties.class).getSerialNumber();
	    }
				
		LOGGER.error("not supported device id template");
		throw new DeviceCredentialsServiceException("not supported device id template");
	}
    
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void requestAndWriteDeviceCredentials() throws AbstractAgentException {
		// stop platform service, set bootstrap credentials (don't persist) and start platform service
        if (platformService.isStarted()) {
            platformService.stop();
        }
        credentialsManager.setCredentials(configuration.getBootstrapCredentials());
		platformService.start();
		
		// get credentials from platform and stop platform service
		AgentCredentials credentials = platformService.getDeviceCredentials(getDeviceId(), configuration.getInterval());
		credentialsManager.writeCredentials(credentials);
	    
	    // stop platform service
		platformService.stop();
	}

}
