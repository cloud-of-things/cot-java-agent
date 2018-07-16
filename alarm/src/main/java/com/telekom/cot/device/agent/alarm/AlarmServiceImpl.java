package com.telekom.cot.device.agent.alarm;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AlarmServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.service.AbstractAgentService;

public class AlarmServiceImpl extends AbstractAgentService implements AlarmService {

	/** the logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AlarmServiceImpl.class);

	@Inject
	private PlatformService platformService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws AbstractAgentException {
		if (isStarted()) {
			throw new AlarmServiceException("the alarm service was already started");
		}

		LOGGER.debug("start " + this.getClass().getSimpleName());

		AssertionUtil.assertNotNull(platformService, AlarmServiceException.class, LOGGER, "no platform service given");

		super.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() throws AbstractAgentException {
		if (isStarted()) {
			LOGGER.debug("stop " + this.getClass().getSimpleName());

			super.stop();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createAlarm(String type, AlarmSeverity severity, String text, String status)
			throws AbstractAgentException {
		LOGGER.debug("create and send alarm of type {} with text {} and severity {}", type, text, severity.getValue());
		platformService.createAlarm(new Date(), type, severity, text, status);
	}

}
