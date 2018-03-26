package com.telekom.cot.device.agent.service.configuration.testconfigs;

public class AgentConfigPart {

	private PlatformConfigPart platform = new PlatformConfigPart();
	private ServiceConfigPart testService1 = new ServiceConfigPart();
	private ServiceConfigPart testService2 = new ServiceConfigPart();
	
	public PlatformConfigPart getPlatform() {
		return platform;
	}

	public void setPlatform(PlatformConfigPart platform) {
		this.platform = platform;
	}

	public ServiceConfigPart getTestService1() {
		return testService1;
	}

	public void setTestService1(ServiceConfigPart testService1) {
		this.testService1 = testService1;
	}

	public ServiceConfigPart getTestService2() {
		return testService2;
	}

	public void setTestService2(ServiceConfigPart testService2) {
		this.testService2 = testService2;
	}
}
