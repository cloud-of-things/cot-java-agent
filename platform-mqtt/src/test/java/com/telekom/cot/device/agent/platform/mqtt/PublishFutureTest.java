package com.telekom.cot.device.agent.platform.mqtt;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;

//import com.telekom.cot.device.agent.common.injection.InjectionUtil;

public class PublishFutureTest {
	public class TestType {
		
	}
	// class getting tested
	private PublishFuture<TestType> publishFuture;
	
	// used mock
	@Mock
	private Future<TestType> mockFuture;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		publishFuture = new PublishFuture<TestType>(mockFuture);
	}
	
	@Test
	public void testCancel() {
		// arrange
		when(mockFuture.cancel(true)).thenReturn(true);
		
		// act
		boolean test = publishFuture.cancel(true);
		
		// assert
		verify(mockFuture, times(1)).cancel(true);
		assertTrue(test);
	}
	
	@Test
	public void testIsCancelled() {
		// arrange
		when(mockFuture.isCancelled()).thenReturn(true);
		
		// act 
		boolean test = publishFuture.isCancelled();
		
		// assert
		verify(mockFuture, times(1)).isCancelled();
		assertTrue(test);
	}
	
	@Test
	public void testIsDone() {
		// arrange
		when(mockFuture.isDone()).thenReturn(true);
		
		// act 
		boolean test = publishFuture.isDone();
		
		// assert
		verify(mockFuture, times(1)).isDone();
		assertTrue(test);
	}
	
	@Test
	public void testGetWithoutTimeout() throws InterruptedException, ExecutionException, AbstractAgentException {
		// arrange 
		when(mockFuture.get()).thenReturn(new TestType());
		
		// act
		Object test = publishFuture.get();
		
		// assert
		verify(mockFuture, times(1)).get();
		assertTrue(test instanceof TestType);
	}
	
	@Test(expected = PlatformServiceException.class)
	public void testGetWithoutTimeout_EXCEPTION() throws InterruptedException, ExecutionException, AbstractAgentException {
		// arrange 
		when(mockFuture.get()).thenThrow(InterruptedException.class);
		
		// act
		publishFuture.get();
		
		// assert
		verify(mockFuture, times(1)).get();
	}
	
	@Test
	public void testGetWithTimeout() throws InterruptedException, ExecutionException, AbstractAgentException, TimeoutException {
		// arrange 
		when(mockFuture.get(Mockito.anyLong(), Mockito.any())).thenReturn(new TestType());
		long timeout = 500;
		
		// act
		Object test = publishFuture.get(timeout, TimeUnit.MILLISECONDS);
		
		// assert
		verify(mockFuture, times(1)).get(timeout, TimeUnit.MILLISECONDS);
		assertTrue(test instanceof TestType);
	}
	
	@Test(expected = PlatformServiceException.class)
	public void testGetWithTimeout_EXCEPTION() throws InterruptedException, ExecutionException, AbstractAgentException, TimeoutException {
		// arrange 
		when(mockFuture.get(Mockito.anyLong(), Mockito.any())).thenThrow(InterruptedException.class);
		long timeout = 500;
		
		// act
		Object test = publishFuture.get(timeout, TimeUnit.MILLISECONDS);
		
		// assert
		verify(mockFuture, times(1)).get(timeout, TimeUnit.MILLISECONDS);
	}
	
}
