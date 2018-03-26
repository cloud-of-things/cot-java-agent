package com.telekom.m2m.cot.restsdk.devicecontrol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestOperationCollection extends OperationCollection {

	private List<Operation> operations = new ArrayList<>();

	public TestOperationCollection(Operation... operations) {
		super(null, null, null, null, 0);
		this.operations.addAll(Arrays.asList(operations));
	}

	@Override
	public Operation[] getOperations() {
		return operations.toArray(new Operation[] {});
	}

	@Override
	public boolean hasNext() {
		operations.clear();
		return false;
	}

}