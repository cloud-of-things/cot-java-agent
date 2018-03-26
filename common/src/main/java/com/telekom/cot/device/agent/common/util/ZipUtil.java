package com.telekom.cot.device.agent.common.util;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtil.class);

    public static void unzip(URI zipFile, Path outputDirectory) throws IOException {
        byte[] zipFileData = Files.readAllBytes(Paths.get(zipFile));
        unzip(zipFileData, outputDirectory);
    }
    
    public static void unzip(byte[] zipFileData, Path outputDirectory) throws IOException {
        LOGGER.info("unzip data to {}", outputDirectory);
        ByteArrayInputStream bais = new ByteArrayInputStream(zipFileData);
        ZipInputStream zipInputStream = new ZipInputStream(bais);

        // unzip entries
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            String outpath = Paths.get(outputDirectory.toAbsolutePath().toString(), entry.getName()).toString();
            LOGGER.debug("unzip entry " + outpath);
            FileOutputStream output = new FileOutputStream(outpath);
            // create a buffer to copy through
            byte[] buffer = new byte[2048];
            // now copy out of the zip archive until all bytes are copied
            int len;
            while ((len = zipInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, len);
            }
            // close stream
            output.close();
        }
    }

/*    
*/
}
