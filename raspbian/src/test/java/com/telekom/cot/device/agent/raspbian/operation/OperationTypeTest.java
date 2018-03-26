package com.telekom.cot.device.agent.raspbian.operation;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.telekom.cot.device.agent.raspbian.operation.RaspbianOperationsHandler.OperationType;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;

public class OperationTypeTest {

	@Test
	public void testFindByAttributes() {

		Operation operation = new Operation();
		operation.set("c8y_Restart", new JsonObject());
		operation.set("whatEver", "-");

		OperationType operationType = OperationType.findByAttributes(operation.getAttributes().keySet());

		Assert.assertThat(operationType, Matchers.equalTo(OperationType.C8Y_RESTART));
		
		System.out.println(OperationType.attributes());

	}

}
