package com.telekom.cot.device.agent.common.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.telekom.cot.device.agent.common.ChecksumAlgorithm;

public class ChecksumUtilTest {
    
    private static Path tempDirectory;
    private static File debianFile;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        tempDirectory = Files.createTempDirectory("zip_test_");
        ZipUtil.unzip(ZipUtil.class.getResource("/device-agent-demo_0.9.0~SNAPSHOT_all.deb.zip").toURI(), tempDirectory);
        debianFile = Paths.get(tempDirectory.toString(), "device-agent-app-0.9.0-SNAPSHOT.deb").toFile();
    }

    @AfterClass
    public static void tearDownClass() {
        FileUtil.deleteDirectory(tempDirectory);
    }

    @Test
    public void testVerify_MD5() throws Exception {
        assertTrue(ChecksumUtil.verifyFile(debianFile, ChecksumAlgorithm.MD5));
    }

    @Test
    public void testVerify_SHA1() throws Exception {
        assertTrue(ChecksumUtil.verifyFile(debianFile, ChecksumAlgorithm.SHA1));
    }

    @Test
    public void testVerify_SHA256() throws Exception {
        assertTrue(ChecksumUtil.verifyFile(debianFile, ChecksumAlgorithm.SHA256));
    }
}
