package com.telekom.cot.device.agent.raspbian.system;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.telekom.cot.device.agent.common.exc.SystemServiceException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.FirmwareProperties;
import com.telekom.cot.device.agent.system.properties.HardwareProperties;

public class RaspbianSystemServiceTest {

    private static final String HARDWARE = "BCM 2709";
	private static final String REVISION = "0815";
	private static final String SERIAL_NUMBER = "987654321";
	private static final String OS_NAME = "Raspbian";
	private static final String OS_VERSION = "9.3";

	private static final String[] READ_HARDWARE_COMMAND = { "/bin/sh", "-c", RaspbianSystemService.READ_HARDWARE_COMMAND };
    private static final String[] READ_REVISION_COMMAND = { "/bin/sh", "-c", RaspbianSystemService.READ_REVISION_COMMAND };
    private static final String[] READ_SERIAL_NUMBER_COMMAND = { "/bin/sh",  "-c", RaspbianSystemService.READ_SERIAL_NUMBER_COMMAND };
    private static final String[] READ_OS_NAME_COMMAND = { "/bin/sh", "-c", RaspbianSystemService.READ_OS_NAME_COMMAND };
    private static final String[] READ_OS_VERSION_COMMAND = { "/bin/sh", "-c", RaspbianSystemService.READ_OS_VERSION_COMMAND };

	@Mock
	private Runtime mockRuntime;
	@Mock
	private Process mockProcessHardware;
	@Mock
	private Process mockProcessRevision;
	@Mock
	private Process mockProcessSerialNumber;
	@Mock
	private Process mockProcessOsName;
	@Mock
	private Process mockProcessOsVersion;
	@Mock
	private ConfigurationManager mockConfigurationManager;
	@Mock
	private HardwareProperties mockHardwareProperties;
	@Mock
	private AgentServiceProvider mockServiceProvider;

	private RaspbianSystemService raspbianSystemService = new RaspbianSystemService();

	@Before
	public void setUp() throws Exception {
		// init and inject mocks
		MockitoAnnotations.initMocks(this);
		InjectionUtil.inject(raspbianSystemService, mockRuntime);
		InjectionUtil.inject(raspbianSystemService, mockConfigurationManager);
		InjectionUtil.inject(raspbianSystemService, mockServiceProvider);
		
		// behavior of mocked Runtime
		when(mockRuntime.exec(READ_HARDWARE_COMMAND)).thenReturn(mockProcessHardware);
		when(mockRuntime.exec(READ_REVISION_COMMAND)).thenReturn(mockProcessRevision);
		when(mockRuntime.exec(READ_SERIAL_NUMBER_COMMAND)).thenReturn(mockProcessSerialNumber);
		when(mockRuntime.exec(READ_OS_NAME_COMMAND)).thenReturn(mockProcessOsName);
		when(mockRuntime.exec(READ_OS_VERSION_COMMAND)).thenReturn(mockProcessOsVersion);

		// behavior of mocked Processes
		when(mockProcessHardware.getInputStream()).thenReturn(toInputStream(HARDWARE));
		when(mockProcessRevision.getInputStream()).thenReturn(toInputStream(REVISION));
		when(mockProcessSerialNumber.getInputStream()).thenReturn(toInputStream(SERIAL_NUMBER));
		when(mockProcessOsName.getInputStream()).thenReturn(toInputStream(OS_NAME));
		when(mockProcessOsVersion.getInputStream()).thenReturn(toInputStream(OS_VERSION));
		
		// behavior of mocked ServiceProvider
		when(mockServiceProvider.getService(SystemService.class)).thenReturn(raspbianSystemService);
	}

	/**
	 * Test method start
	 */
	@Test
	public void testStart() throws Exception {
		raspbianSystemService.start();
	}

	/**
	 * Test method stop
	 */
	@Test
	public void testStop() throws Exception {
		raspbianSystemService.stop();
	}

	/**
	 * Test method start, Runtime.exec throws Exception
	 */
	@Test(expected = SystemServiceException.class)
	public void testStartRuntimeExecException() throws Exception {
		// Throw exception on Runtime.exec call
		doThrow(new IOException()).when(mockRuntime).exec(READ_HARDWARE_COMMAND);

		raspbianSystemService.start();
	}

	/**
	 * Test method start, Runtime.exec returns no process instance
	 */
	@Test(expected = SystemServiceException.class)
	public void testStartNoProcess() throws Exception {
		// Return null on Runtime.exec call
		when(mockRuntime.exec(READ_HARDWARE_COMMAND)).thenReturn(null);
		raspbianSystemService.start();
	}

	/**
	 * Test method start, Process.getInputStream returns null
	 */
	@Test(expected = SystemServiceException.class)
	public void testStartNoInputStream() throws Exception {
		// Return null on Process.getInputStream call
		when(mockProcessHardware.getInputStream()).thenReturn(null);
		raspbianSystemService.start();
	}

	/**
	 * Test method start, InputStream.read throws IOException
	 */
	@Test(expected = SystemServiceException.class)
	public void testStartInputStreamException() throws Exception {
		// Throw IOException on InputStream.read call
		InputStream mockInputStream = mock(InputStream.class);
		when(mockProcessHardware.getInputStream()).thenReturn(mockInputStream);
		doThrow(new IOException()).when(mockInputStream).read();

		raspbianSystemService.start();
	}

	/**
	 * Test complete
	 */
	@Test
	public void testComplete() throws Exception {
		raspbianSystemService.start();

		HardwareProperties hardwareProperties = raspbianSystemService.getProperties(HardwareProperties.class);
		FirmwareProperties firmwareProperties = raspbianSystemService.getProperties(FirmwareProperties.class);

		assertEquals(HARDWARE, hardwareProperties.getModel());
		assertEquals(REVISION, hardwareProperties.getRevision());
		assertEquals(SERIAL_NUMBER, hardwareProperties.getSerialNumber());
		assertEquals(OS_NAME, firmwareProperties.getName());
		assertEquals(OS_VERSION, firmwareProperties.getVersion());
		assertNull(firmwareProperties.getUrl());

	}

	private InputStream toInputStream(String string) {
		return new ByteArrayInputStream(string.getBytes());
	}
}
