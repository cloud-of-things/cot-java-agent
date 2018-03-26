package com.telekom.cot.device.agent.common.util;

import static org.junit.Assert.*;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZipUtilTest {

    private Path tempDirectory;
    
    @Before
    public void setUp() throws Exception {
        tempDirectory = Files.createTempDirectory("zip_test_");
    }
    
    @After
    public void tearDown() {
        FileUtil.deleteDirectory(tempDirectory);
    }
    
    @Test
    public void testUnzip() throws Exception {
        URI zipFile = ZipUtil.class.getResource("/device-agent-demo_0.9.0~SNAPSHOT_all.deb.zip").toURI();
        
        ZipUtil.unzip(zipFile, tempDirectory);
        
        Path debFile = Paths.get(tempDirectory.toString(), "device-agent-app-0.9.0-SNAPSHOT.deb");
        Path md5File = Paths.get(tempDirectory.toString(), "device-agent-app-0.9.0-SNAPSHOT.deb.md5");
        Path sha1File = Paths.get(tempDirectory.toString(), "device-agent-app-0.9.0-SNAPSHOT.deb.sha1");
        Path sha256File = Paths.get(tempDirectory.toString(), "device-agent-app-0.9.0-SNAPSHOT.deb.sha256");

        assertTrue(Files.exists(debFile));
        assertTrue(Files.exists(md5File));
        assertTrue(Files.exists(sha1File));
        assertTrue(Files.exists(sha256File));
    }
}
