package com.telekom.cot.device.agent.operation.softwareupdate;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.createExceptionAndLog;

import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.SoftwareInstallerException;

public class SoftwareInstallerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoftwareInstallerFactory.class);

    /**
     * get an instance of {@code SoftwareInstaller} depending on the current running operating system
     * @return a new instance of {@code SoftwareInstaller}
     * @throws AbstractAgentException if current operating system is not supported
     */
    public static SoftwareInstaller getInstance() throws AbstractAgentException {
        // get installer depending on operating system
        if (isDebian()) {
            LOGGER.debug("debian operating system, get {}", DebianSoftwareInstaller.class.getName());
            return new DebianSoftwareInstaller();
        }
        
        if (isWindows()) {
            LOGGER.debug("windows operating system, get {}", WindowsTestSoftwareInstaller.class.getName());
            return new WindowsTestSoftwareInstaller();
        }
        
        throw createExceptionAndLog(SoftwareInstallerException.class, LOGGER,
                        "can't get software installer instance, not supported operating system");
    }
    
    /**
     * check whether the running operating system is a linux system
     * @return whether operating system is linux
     */
    private static boolean isLinux() {
        // check whether operation system is a linux system
        String osName = System.getProperty("os.name");
        return Objects.nonNull(osName) && osName.startsWith("Linux");
    }

    /**
     * check whether the running operating system is a windows system
     * @return whether operating system is windows
     */
    private static boolean isWindows() {
        // check whether operation system is a linux system
        String osName = System.getProperty("os.name");
        return Objects.nonNull(osName) && osName.startsWith("Windows");
    }

    /**
     * check whether current operating 
     * @return
     */
    private static boolean isDebian() {
        // check whether operation system is a linux system
        if (!isLinux()) {
            LOGGER.debug("operating system is no linux system");
            return false;
        }
        
        // check debian or raspian operation system
        Runtime runtime = Runtime.getRuntime();
        try {
            String[] command = { "/bin/sh", "-c", "cat /etc/os-release | grep ID | grep -v VERSION | cut -d= -f2" };
            Process process = runtime.exec(command);
            String osId = IOUtils.toString(process.getInputStream(), "UTF-8");
            LOGGER.debug("os id = {}", osId);
            return osId.contains("debian");
        } catch(Exception e) {
            LOGGER.error("can't get operation system id", e);
            return false;
        }
    }
}
