package com.telekom.cot.device.agent.common.configuration;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;

@ConfigurationPath(value = "agent.credentials")
public class AgentCredentials implements Configuration {

    @NotNull
    @NotEmpty
    private String tenant;
    @NotNull
    @NotEmpty
    private String username;
    @NotNull
    @NotEmpty
    private String password;
    private boolean bootstrapping;

    public AgentCredentials() {
        this.bootstrapping = false;
    }

    public AgentCredentials(boolean bootstrapping, String tenant, String username, String password) {
        this.bootstrapping = bootstrapping;
        this.tenant = tenant;
        this.username = username;
        this.password = password;
    }

    public boolean isBootstrappingMode() {
        return bootstrapping;
    }

    public void setBootstrappingMode(boolean bootstrapping) {
        this.bootstrapping = bootstrapping;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return AgentCredentials.class.getSimpleName() + " [tenant=" + tenant + ", username=" + username + ", password="
                        + password + ", bootstrapping=" + bootstrapping + "]";
    }
}
