package com.telekom.cot.device.agent.operation.softwareupdate;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.assertNotNull;
import static com.telekom.cot.device.agent.common.util.AssertionUtil.createExceptionAndLog;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.ChecksumAlgorithm;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.SoftwareInstallerException;
import com.telekom.cot.device.agent.common.util.ChecksumUtil;
import com.telekom.cot.device.agent.common.util.ZipUtil;
import com.telekom.cot.device.agent.platform.PlatformService;

public abstract class SoftwareInstaller {
    
    private static final String AGENT_CONFIGURATION_FILE = "agent.yaml";

    private static final Logger LOGGER = LoggerFactory.getLogger(SoftwareInstaller.class);
    
    private Path tempDownloadDir;
    
    /**
     * protected constructor, creates the download target path if not existent
     * @param downloadTargetPath path where downloaded software is stored
     * @throws AbstractAgentException 
     */
    protected SoftwareInstaller() throws AbstractAgentException {
        try {
            // create temporary directory for download
            tempDownloadDir = Files.createTempDirectory("agent-update-");
            LOGGER.debug("created temporary download directory '{}'", tempDownloadDir.toString());
        } catch (Exception e) {
            throw createExceptionAndLog(SoftwareInstallerException.class, LOGGER, "can't create temporary download directory", e);
        }
    }

    /**
     * get the current temporary download directory
     */
    public Path getTemporaryDownloadDirectory() {
        return tempDownloadDir;
    }

    /**
     * download the software at given url by given platform service and verify
     * downloaded file by given checksum algorithm 
     * @param platformService platform service instance to use for download
     * @param downloadUrl url of the software binary to download
     * @param checksumAlgorithm checksum algorithm to verify downloaded binary
     * @throws AbstractAgentException
     */
    public void downloadSoftware(PlatformService platformService, URL downloadUrl, ChecksumAlgorithm checksumAlgorithm) throws AbstractAgentException {
        // check platform service, download url and checksum algorithm
        assertNotNull(platformService, SoftwareInstallerException.class, LOGGER, "no platform service given");
        assertNotNull(downloadUrl, SoftwareInstallerException.class, LOGGER, "no download url given");
        assertNotNull(checksumAlgorithm, SoftwareInstallerException.class, LOGGER, "no checksum algorithm given");
        
        // download software by platform service and unzip downloaded binary
        byte[] zipFileBinary = platformService.downloadBinary(downloadUrl);
        unzipBinary(zipFileBinary);
        
        // get debian package file and verify checksum 
        File debianPackageFile = getDebianPackageFile();
        verifyFile(debianPackageFile, checksumAlgorithm);
    }
    
    /**
     * install the previous downloaded software
     * @throws AbstractAgentException
     */
    public abstract void installSoftware() throws AbstractAgentException;

    /**
     * backup the local agent configuration file
     * @throws AbstractAgentException 
     */
    protected void backupAgentConfiguration() throws AbstractAgentException {
        Path agentConfigurationFilePath;
        try {
            // copy agent configuration file from current working dir to download dir
            String currentWorkingDir = System.getProperty("user.dir");
            agentConfigurationFilePath = Paths.get(currentWorkingDir, AGENT_CONFIGURATION_FILE);
        } catch (Exception e) {
            throw createExceptionAndLog(SoftwareInstallerException.class, LOGGER,
                            "can't get agent configuration file path at current working directory", e);
        }
        
        try {
            // copy agent configuration file
            Files.copy(agentConfigurationFilePath, Paths.get(tempDownloadDir.toString(), AGENT_CONFIGURATION_FILE));
        } catch (Exception e) {
            throw createExceptionAndLog(SoftwareInstallerException.class, LOGGER,
                            "can't backup agent configuration at '" + agentConfigurationFilePath + "'", e);
        }
    }
    
    /**
     * unzip the given binary zip file
     */
    private void unzipBinary(byte[] zipFileBinary) throws AbstractAgentException {
        try {
            ZipUtil.unzip(zipFileBinary, tempDownloadDir);
        } catch (Exception e) {
            throw createExceptionAndLog(SoftwareInstallerException.class, LOGGER, "can't unzip downloaded file", e);
        }
    }
    
    /**
     * get the debian package file (*.deb) at temporary download directory  
     */
    private File getDebianPackageFile() throws AbstractAgentException {
        try {
            File[] files = tempDownloadDir.toFile().listFiles();
            for (File file : files) {
                if (file.getName().toLowerCase().endsWith(".deb")) {
                    return file;
                }
            }
            
            throw createExceptionAndLog(SoftwareInstallerException.class, LOGGER, 
                            "can't find debian package file at " + tempDownloadDir.toString());
        } catch(Exception e) {
            throw createExceptionAndLog(SoftwareInstallerException.class, LOGGER,
                            "can't find debian package file at " + tempDownloadDir.toString(), e);
        }
    }
    
    /**
     * verify the checksum of given file by given algorithm
     */
    private void verifyFile(File file, ChecksumAlgorithm checksumAlgorithm) throws AbstractAgentException {
        try {
            if(!ChecksumUtil.verifyFile(file, checksumAlgorithm)) {
                throw createExceptionAndLog(SoftwareInstallerException.class, LOGGER,
                                "checksum (" + checksumAlgorithm.toString() + ") of downloaded file is not valid");
            }
        } catch (Exception e) {
            throw createExceptionAndLog(SoftwareInstallerException.class, LOGGER,
                            "can't verify checksum of debian package file", e);
        }
    }
}
