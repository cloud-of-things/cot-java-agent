package com.telekom.cot.device.agent.event;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.EventServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.service.AbstractAgentService;

public class EventServiceImpl extends AbstractAgentService implements EventService {

	/** the logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceImpl.class);
	
	@Inject
	private PlatformService platformService;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws AbstractAgentException {
		if (isStarted()) {
			throw new EventServiceException("the event service was already started");
		}

		LOGGER.debug("start " + this.getClass().getSimpleName());
        AssertionUtil.assertNotNull(platformService, EventServiceException.class, LOGGER, "no platform service given");
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
	public void createEvent(String type, String text, String condition)
			throws AbstractAgentException {
		LOGGER.debug("create and send event of type '{}' with text '{}'", type, text);
		platformService.createEvent(new Date(), type, text, condition);
	}
}
