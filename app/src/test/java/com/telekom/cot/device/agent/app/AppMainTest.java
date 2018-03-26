package com.telekom.cot.device.agent.app;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

public class AppMainTest {

	@Mock
	Logger logger;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

//	@Test
//	public void testMain() throws Exception {
//		Field field = AppMain.class.getDeclaredField("LOGGER");
//		field.setAccessible(true);
//		field.set(null, logger);
//
//		try {
//			AppMain.main(new String[] { "pathToLogFile" });
//			Assert.fail();
//		} catch (AppMainException exception) {
//
//		}
//		verify(logger).info(eq("start CoT device agent app, process id = {}"), any(Long.class));
//	}

	@Test(expected = IllegalArgumentException.class)
	public void testMainWithoutArguments() throws Exception {
		AppMain.main(new String[] {});
	}
}
