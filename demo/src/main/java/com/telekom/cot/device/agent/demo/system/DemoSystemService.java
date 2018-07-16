package com.telekom.cot.device.agent.demo.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.system.AbstractSystemService;
import com.telekom.cot.device.agent.system.properties.FirmwareProperties;
import com.telekom.cot.device.agent.system.properties.HardwareProperties;

public class DemoSystemService extends AbstractSystemService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemoSystemService.class);

	@Inject
	private DemoHardwareProperties hardwareProperties;
	
	@Inject
	private DemoFirmwareProperties firmwareProperties;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws AbstractAgentException {
		setProperties(HardwareProperties.class, hardwareProperties);
		setProperties(FirmwareProperties.class, firmwareProperties);
		
		LOGGER.info("started {}", DemoSystemService.class.getSimpleName());
		super.start();
	}
}
