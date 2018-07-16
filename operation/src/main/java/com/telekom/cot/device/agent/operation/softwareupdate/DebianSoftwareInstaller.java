package com.telekom.cot.device.agent.operation.softwareupdate;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.createExceptionAndLog;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.SoftwareInstallerException;

class DebianSoftwareInstaller extends SoftwareInstaller {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DebianSoftwareInstaller.class);
    
    private static final String UPDATE_SCRIPT_RESOURCE_PATH = "scripts/debian/update.sh";
    private final Path UPDATE_SCRIPT_FILE_PATH;

    DebianSoftwareInstaller() throws AbstractAgentException {
        super();
        UPDATE_SCRIPT_FILE_PATH = Paths.get(getTemporaryDownloadDirectory().toString(), "update.sh");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void installSoftware() throws AbstractAgentException {
        backupAgentConfiguration();
        getUpdateScript();
        setUpdateScriptExecutable();
        executeUpdateScript();
    }
    
    /**
     * get the update script from within jar file and copy it to local file system
     */
    private void getUpdateScript() throws AbstractAgentException {
        try {
            // copy update script
            InputStream updateScriptStream = DebianSoftwareInstaller.class.getClassLoader().getResourceAsStream(UPDATE_SCRIPT_RESOURCE_PATH);
            Files.copy(updateScriptStream, UPDATE_SCRIPT_FILE_PATH);
            LOGGER.info("copied update script from jar file to {}", UPDATE_SCRIPT_FILE_PATH);
        } catch (Exception e) {
            throw createExceptionAndLog(SoftwareInstallerException.class, LOGGER, "can't copy update script from jar file to " + UPDATE_SCRIPT_FILE_PATH, e);
        }
    }
    
    /**
     * set file permissions to make update script executable
     */
    private void setUpdateScriptExecutable() throws AbstractAgentException {
        try {
            // set execute permissions
            Set<PosixFilePermission> filePermissions = Files.getPosixFilePermissions(UPDATE_SCRIPT_FILE_PATH);
            filePermissions.add(PosixFilePermission.OWNER_EXECUTE);
            filePermissions.add(PosixFilePermission.GROUP_EXECUTE);
            filePermissions.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(UPDATE_SCRIPT_FILE_PATH, filePermissions);
            LOGGER.info("set update script execute permissions");
        } catch (Exception e) {
            throw createExceptionAndLog(SoftwareInstallerException.class, LOGGER, "can't set update script execute permissions", e);
        }
    }
    
    /**
     * execute the update script
     */
    private void executeUpdateScript() throws AbstractAgentException {
        Runtime currentRuntime = Runtime.getRuntime();
        try {
            LOGGER.debug("start update agent");
            
            String[] command = { "/bin/sh", "-c", "cd " + getTemporaryDownloadDirectory().toString() + " && ./update.sh" };
            currentRuntime.exec(command).waitFor();
            LOGGER.debug("agent update done");
        } catch(Exception e) {
            throw createExceptionAndLog(SoftwareInstallerException.class, LOGGER, "can't run update process", e);
        }
    }
}
