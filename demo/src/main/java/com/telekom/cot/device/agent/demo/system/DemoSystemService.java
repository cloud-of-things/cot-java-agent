package com.telekom.cot.device.agent.demo.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.system.AbstractSystemService;
import com.telekom.cot.device.agent.system.properties.FirmwareProperties;
import com.telekom.cot.device.agent.system.properties.HardwareProperties;

public class DemoSystemService extends AbstractSystemService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemoSystemService.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws AbstractAgentException {
		setProperties(HardwareProperties.class,
				getConfigurationManager().getConfiguration(DemoHardwareProperties.class));
		setProperties(FirmwareProperties.class,
				getConfigurationManager().getConfiguration(DemoFirmwareProperties.class));
		
		LOGGER.info("started {}", DemoSystemService.class.getSimpleName());
		super.start();
	}
}
