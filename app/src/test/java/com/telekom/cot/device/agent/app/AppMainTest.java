package com.telekom.cot.device.agent.app;

import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.injection.InjectionUtil;

public class AppMainTest {

	@Mock
	Logger logger;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testMain() throws Exception {
		InjectionUtil.injectStatic(AppMain.class, logger);
		try {
			AppMain.main(new String[] { "pathToLogFile" });
			Assert.fail();
		} catch (AppMainException exception) {
		}
		verify(logger).info(Mockito.eq("start CoT device agent app, process id = {}"), Mockito.any(Long.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMainWithoutArguments() throws Exception {
		AppMain.main(new String[] {});
	}
}
