package com.telekom.cot.device.agent.app;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.credentials.DeviceCredentialsServiceConfiguration;
import com.telekom.cot.device.agent.platform.mqtt.PlatformServiceMqttConfiguration;
import com.telekom.cot.device.agent.platform.rest.PlatformServiceRestConfiguration;

public class ConnectivityChecker {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectivityChecker.class);

    /**
     * check connectivity to platform
     * @param configurationManager valid configuration manager instance to get connectivity timeout, hostname,...
     * @throws AppMainException if an error occurs or connectivity check is timed out 
     */
    public static void checkPlatformConnectivity(ConfigurationManager configurationManager) throws AppMainException {
        // get connectivity timeout from common configuration and check
        int connectivityTimeout = getConnectivityTimeout(configurationManager);
        if (connectivityTimeout == 0) {
            LOGGER.info("won't check platform connectivity, connectivity timeout 0 configured");
            return;
        }
        
        // try to get hostname from platform service (mqtt/rest) configuration 
        String hostname = getHostnameMqtt(configurationManager);
        if(StringUtils.isEmpty(hostname)) {
            hostname = getHostnameRest(configurationManager);
        }

        // check connection every 15 seconds, 4 times timeout (in minutes)
        connectivityTimeout *= 4;
        while (connectivityTimeout < 0 || connectivityTimeout > 0) {
            if (connect(hostname, 443)) {
                LOGGER.info("connected successfully to platform");
                return;
            }
            
            connectivityTimeout--;
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
            }
        }
        
        throw logErrorAndCreateException("check platform connectivity timed out, platform not reachable", null);
    }
    
    /**
     * log the given error message and exception and create a AppMainException with given message and exception
     */
    private static AppMainException logErrorAndCreateException(String errorMessage, Exception e) {
        if (Objects.nonNull(e)) {
            LOGGER.error(errorMessage, e);
            return new AppMainException(errorMessage, e);
        }

        LOGGER.error(errorMessage);
        return new AppMainException(errorMessage);
    }
 
    /**
     * get the connectivity timeout from common configuration
     */
    private static int getConnectivityTimeout(ConfigurationManager configurationManager) throws AppMainException {
        try {
            CommonConfiguration configuration = configurationManager.getConfiguration(CommonConfiguration.class);
            return configuration.getConnectivityTimeout();
        } catch (Exception e) {
            throw logErrorAndCreateException("can't get connectivity timeout from common configuration", e);
        }
        
    }
    
    /**
     * get hostname from platform service mqtt configuration
     */
    private static String getHostnameMqtt(ConfigurationManager configurationManager) {
        // try to get hostname by PlatformServiceMqttConfiguration
        try {
            PlatformServiceMqttConfiguration configuration = configurationManager.getConfiguration(PlatformServiceMqttConfiguration.class);
            return configuration.getHostName();
        } catch (Exception e) {
            LOGGER.debug("can't get hostname from platform service mqtt configuration", e);
            return null;
        }
    }

    /**
     * get hostname from platform service rest configuration
     */
    private static String getHostnameRest(ConfigurationManager configurationManager) throws AppMainException {
        // try to get bootstrap tenant from device credentials service configuration
        String bootstrapTenant = null;
        try {
            DeviceCredentialsServiceConfiguration configuration = configurationManager.getConfiguration(DeviceCredentialsServiceConfiguration.class);
            bootstrapTenant = configuration.getBootstrapCredentials().getTenant();
        } catch (Exception e) {
            throw logErrorAndCreateException("can't get bootstrap tenant from device credentials service configuration", e);
        }

        // check bootstrap tenant
        if (StringUtils.isEmpty(bootstrapTenant)) {
            throw logErrorAndCreateException("can't get bootstrap tenant from device credentials service configuration", null);
        }
        
        // try to get hostname by PlatformServiceRestConfiguration
        try {
            PlatformServiceRestConfiguration configuration = configurationManager.getConfiguration(PlatformServiceRestConfiguration.class);
            return bootstrapTenant + "." + configuration.getHostName();
        } catch (Exception e) {
            throw logErrorAndCreateException("can't get hostname from platform service rest configuration", e);
        }
    }

    /**
     * connect to given host at given port
     */
    private static boolean connect(String host, int port) {
        try {
            InetAddress address = InetAddress.getByName(host);
            Socket socket = new Socket(address, port);
            socket.close();
            LOGGER.debug("connected successfully to {}({}):{}", host, address.getHostAddress(), port);
            return true;
        } catch(Exception e) {
            LOGGER.error("can't connected {}:{}", host, port);
            return false;
        }
    }
}
