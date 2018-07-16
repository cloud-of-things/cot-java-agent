package com.telekom.cot.device.agent.app;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManagerImpl;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManagerImpl;
import com.telekom.cot.device.agent.service.AgentServiceManagerImpl;

public class AppMain {

    public enum RunningState {
        UNKNOWN("UNKNOWN"),
        STARTED("STARTED"),
        RUNNING("RUNNING");
        
        String value;
        
        RunningState(String value) {
            this.value = value;
        }
    }
    
    /** Vertx SLF4J log factory. */
	private static final String SLF4J_LOG_DELEGATE_FACTORY = "io.vertx.core.logging.SLF4JLogDelegateFactory";
	/** Vertx delegate factory. */
    private static final String VERTX_LOGGER_FACTORY = "vertx.logger-delegate-factory-class-name";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AppMain.class);
	private static final String DEVICE_CREDENTIALS_FILE = "device-credentials.yaml";

	public static void main(String[] args) throws AppMainException {
	    saveAgentRunningState(RunningState.STARTED);
	    
		LOGGER.info("***************************************************************************************************************");
		LOGGER.info("start CoT device agent app, process id = {}", getProcessId());
		LOGGER.info("***************************************************************************************************************");

		if (args.length == 0) {
			LOGGER.error("missing arguments");
			throw new IllegalArgumentException("missing arguments");
		}

		// map the vertx default logging to SLF4J
        System.setProperty(VERTX_LOGGER_FACTORY, SLF4J_LOG_DELEGATE_FACTORY);

        // get configuration manager instance
        ConfigurationManager configurationManager;
        try {
            configurationManager = ConfigurationManagerImpl.getInstance(Paths.get(args[0]).toAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("can't get configuration manager instance", e);
            throw new AppMainException("can't get configuration manager instance", e);
        }
        
        // get credentials manager instance
        AgentCredentialsManager credentialsManager;
        try {
            credentialsManager = AgentCredentialsManagerImpl.getInstance(Paths.get(DEVICE_CREDENTIALS_FILE).toAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("can't get agent credentials manager instance", e);
            throw new AppMainException("can't get agent credentials manager instance", e);
        }
        
		/**
		 * start bootstrapping: configuration, credentials and start services 
		 */
		AppBootstrap.getInstance(AgentServiceManagerImpl.getInstance(), configurationManager, credentialsManager).start();
        saveAgentRunningState(RunningState.RUNNING);
	}

	private static long getProcessId() {
	    long processId = -1;
		try {
			String[] splittedJvmName = ManagementFactory.getRuntimeMXBean().getName().split("@");
			processId = Long.parseLong(splittedJvmName[0]);
		} catch (Exception e) {
			LOGGER.error("can't get process id", e);
			return -1;
		}

		if (processId != -1) {
	        writeToFile(Paths.get("agent.pid"), String.valueOf(processId), "can't write process id to file 'agent.pid'", false);
	    }
	    
        return processId;
	}
	
	private static void saveAgentRunningState(RunningState state) {
	    writeToFile(Paths.get("agent.run"), state.value, "can't write agent running state to 'agent.run'", false);
	}
	
	private static void writeToFile(Path filePath, String text, String errorMessage, boolean append) {
	    FileWriter writer = null;
        try {
            if (!append) {
                Files.deleteIfExists(filePath);
            }
            
            writer = new FileWriter(filePath.toString(), append);
            writer.write(text);
        } catch (Exception e) {
            LOGGER.warn(errorMessage, e);
        } finally {
            if (Objects.nonNull(writer)) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LOGGER.error("can't close writer of file '{}'", filePath.toString(), e);
                }
            }
        }
	}
}
