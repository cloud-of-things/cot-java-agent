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

public class AppMain {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppMain.class);
	private static final String DEVICE_CREDENTIALS_FILE = "device-credentials.yaml";

	public static void main(String[] args) throws AppMainException {

		LOGGER.info("***************************************************************************************************************");
		LOGGER.info("start CoT device agent app, process id = {}", getProcessId());
		LOGGER.info("***************************************************************************************************************");

		if (args.length == 0) {
			LOGGER.error("missing arguments");
			throw new IllegalArgumentException("missing arguments");
		}

		/**
		 * start bootstrapping: configuration, credentials and start services 
		 */
		AppBootstrap.getInstance(args[0], DEVICE_CREDENTIALS_FILE).start();
		saveProcessId();
	}

	private static long getProcessId() {
		try {
			String[] splittedJvmName = ManagementFactory.getRuntimeMXBean().getName().split("@");
			return Long.parseLong(splittedJvmName[0]);
		} catch (Exception e) {
			LOGGER.error("can't get process id", e);
			return -1;
		}
	}
	
	private static void saveProcessId() {
	    long processId = getProcessId();
	    if (processId == -1) {
	        return;
	    }
	    
        FileWriter writer = null;
        try {
            Path filePath = Paths.get("agent.pid");
            Files.deleteIfExists(filePath);
		    writer = new FileWriter(filePath.toString(), false);
		    writer.write(String.valueOf(processId));
		} catch (Exception e) {
			LOGGER.warn("can't write process id to file 'agent.pid'", e);
		} finally {
		    if (Objects.nonNull(writer)) {
		        try {
                    writer.close();
                } catch (IOException e) {
                    LOGGER.error("can't close writer of file 'agent.pid'", e);
                }
		    }
		}
	}
}
