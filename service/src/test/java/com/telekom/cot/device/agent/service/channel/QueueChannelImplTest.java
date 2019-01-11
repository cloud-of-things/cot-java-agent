package com.telekom.cot.device.agent.service.channel;

import java.util.ArrayList;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class QueueChannelImplTest {

	private QueueChannelImpl<String> myQueue;
	String[] testArray = { "Test1", "Test2" };
	ArrayList<String> testList = new ArrayList<String>();

	/**
	 * Setup QueueChannelImpl
	 */
	@Before
	public void setUp() {
		myQueue = new QueueChannelImpl<String>();
	}

	/**
	 * Test Queue for an item
	 */
	@Test
	public void testElement() {
		myQueue.add(testArray[0]);
		Assert.assertThat(myQueue.getItem(), Matchers.equalTo("Test1"));
	}

	/**
	 * Test Queue for an Array
	 */
	@Test
	public void testAddArray() {
		myQueue.add(testArray);
		Assert.assertThat(myQueue.getItem(), Matchers.equalTo("Test1"));
		Assert.assertThat(myQueue.getItem(), Matchers.equalTo("Test2"));
	}
	
	/**
	 * Test Queue for a List
	 */
	@Test
	public void testAddList() {
		testList.add("Test1");
		testList.add("Test2");
		myQueue.add(testList);
		Assert.assertThat(myQueue.getItem(), Matchers.equalTo("Test1"));
		Assert.assertThat(myQueue.getItem(), Matchers.equalTo("Test2"));
	}

	/**
	 * Test if the Queue returns null when empty
	 */
	@Test
	public void testQueueEmpty() {
		Assert.assertNull(myQueue.getItem());
	}

}
