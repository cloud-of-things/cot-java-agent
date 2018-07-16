package com.telekom.cot.device.agent.platform.rest;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.platform.PlatformServiceConfiguration;

@ConfigurationPath("agent.services.platformService")
public class PlatformServiceRestConfiguration extends PlatformServiceConfiguration {

	@NotNull @Valid
	private RestConfiguration restConfiguration;

    public RestConfiguration getRestConfiguration() {
        return restConfiguration;
    }
    
    public void setRestConfiguration(RestConfiguration rest) {
        this.restConfiguration = rest;
    }

    @Override
	public String toString() {
		return PlatformServiceRestConfiguration.class.getSimpleName() + " [hostName=" + getHostName()
		        + ", externalId=" + getExternalIdConfig() 
		        + ", restConfiguration=" + restConfiguration + "]";
	}

	public static class RestConfiguration {

	    private String proxyHost;
	    private String proxyPort;
	    @NotNull @Positive
	    private Integer operationsRequestSize;

	    public String getProxyHost() {
            return proxyHost;
        }

        public void setProxyHost(String proxyHost) {
            this.proxyHost = proxyHost;
        }

        public String getProxyPort() {
            return proxyPort;
        }

        public void setProxyPort(String proxyPort) {
            this.proxyPort = proxyPort;
        }

        public Integer getOperationsRequestSize() {
            return operationsRequestSize;
        }

        public void setOperationsRequestSize(Integer operationsRequestSize) {
            this.operationsRequestSize = operationsRequestSize;
        }

        @Override
		public String toString() {
			return RestConfiguration.class.getSimpleName() + " ["
			                + "proxyHost=" + proxyHost + ", "
			                + "proxyPort=" + proxyPort + ", "
			                + "operationsRequestSize=" + operationsRequestSize + "]";
		}
	}
}
