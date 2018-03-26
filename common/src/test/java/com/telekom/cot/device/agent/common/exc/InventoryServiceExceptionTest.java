package com.telekom.cot.device.agent.common.exc;

import org.junit.Assert;
import org.junit.Test;

public class InventoryServiceExceptionTest {

	@Test
	public void test() {
		InventoryServiceException inventoryServiceException = new InventoryServiceException("testMessage");
		Assert.assertEquals("testMessage", inventoryServiceException.getMessage());

		inventoryServiceException = new InventoryServiceException("testMessage", new Throwable());
		Assert.assertEquals("testMessage", inventoryServiceException.getMessage());

		inventoryServiceException = new InventoryServiceException("testMessage", new Throwable(), true, true);
		Assert.assertEquals("testMessage", inventoryServiceException.getMessage());
	}
}
