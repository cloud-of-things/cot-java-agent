package com.telekom.cot.device.agent.raspbian.system;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.createExceptionAndLog;

import java.io.InputStream;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.SystemServiceException;
import com.telekom.cot.device.agent.system.AbstractSystemService;
import com.telekom.cot.device.agent.system.properties.FirmwareProperties;
import com.telekom.cot.device.agent.system.properties.HardwareProperties;

public class RaspbianSystemService extends AbstractSystemService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RaspbianSystemService.class);
	private Runtime currentRuntime = Runtime.getRuntime();

	/**
	 * commands to read the raspberry Hardware, Revision and Serial Number at
	 * raspbian
	 */
	protected static final String READ_HARDWARE_COMMAND = "cat /proc/cpuinfo | grep Hardware | sed \"s/^.*: //\"";
	protected static final String READ_REVISION_COMMAND = "cat /proc/cpuinfo | grep Revision | sed \"s/^.*: //\"";
	protected static final String READ_SERIAL_NUMBER_COMMAND = "cat /proc/cpuinfo | grep Serial | sed \"s/^.*: //\"";

	/** commands to read the operating system name and version */
	protected static final String READ_OS_NAME_COMMAND = "cat /etc/os-release | grep PRETTY_NAME | cut -d= -f2 | cut -d\\\" -f2";
	protected static final String READ_OS_VERSION_COMMAND = "cat /etc/debian_version";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws AbstractAgentException {
		// read and set hardware and firmware properties
		setProperties(HardwareProperties.class, readHardwareProperties());
		setProperties(FirmwareProperties.class, readFirmwareProperties());

		LOGGER.info("started {}", RaspbianSystemService.class.getSimpleName());
		super.start();
	}

	@Override
	public void stop() throws AbstractAgentException {
		currentRuntime = null;
		super.stop();
	}

	/**
	 * get the Raspbian hardware properties
	 * 
	 * @return Raspbian hardware properties
	 * @throws SystemServiceException
	 *             if properties can't be read
	 */
	private HardwareProperties readHardwareProperties() throws AbstractAgentException {
		String model = executeCommand(READ_HARDWARE_COMMAND);
		String revision = executeCommand(READ_REVISION_COMMAND);
		String serialNumber = executeCommand(READ_SERIAL_NUMBER_COMMAND);

		return new HardwareProperties(model, revision, serialNumber);
	}

	/**
	 * get the Raspbian firmware properties (OS name and version)
	 * 
	 * @return Raspbian firmware properties
	 * @throws SystemServiceException
	 *             if properties can't be read
	 */
	private FirmwareProperties readFirmwareProperties() throws AbstractAgentException {
		String osName = executeCommand(READ_OS_NAME_COMMAND);
		String osVersion = executeCommand(READ_OS_VERSION_COMMAND);

		return new FirmwareProperties(osName, osVersion, null);
	}

	/**
	 * executes a command and returns the output
	 * 
	 * @param shellCommand
	 *            the shell command to execute
	 * @return The output
	 * @throws AbstractAgentException
	 */
	private String executeCommand(String shellCommand) throws AbstractAgentException {

		// execute shell command
		Process process;
		try {
			String[] command = { "/bin/sh", "-c", shellCommand };
			process = currentRuntime.exec(command);
		} catch (Exception e) {
			throw createExceptionAndLog(SystemServiceException.class, LOGGER,
					"can't execute a command '" + shellCommand + "' to get raspbian hardware properties", e);
		}

		// read result of command execution
		try {
			InputStream inputStream = process.getInputStream();
			StringWriter writer = new StringWriter();

			int value;
			while ((value = inputStream.read()) != -1) {
				writer.write(value);
			}

			return writer.toString().replaceAll("\n", "");
		} catch (Exception e) {
			throw createExceptionAndLog(SystemServiceException.class, LOGGER, "can't read output of command execution",
					e);
		}
	}
}
