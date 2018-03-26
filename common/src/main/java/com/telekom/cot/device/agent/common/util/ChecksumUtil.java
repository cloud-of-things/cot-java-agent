package com.telekom.cot.device.agent.common.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.ChecksumAlgorithm;

public class ChecksumUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChecksumUtil.class);

    public static boolean verifyFile(File file, ChecksumAlgorithm checksumAlgorithm) throws IOException, NoSuchAlgorithmException {
        // check file
        if (Objects.isNull(file) || !file.exists()) {
            throw new FileNotFoundException("no file to verify");
        }
        
        // check checksum algorithm
        if (Objects.isNull(checksumAlgorithm)) {
            throw new NoSuchAlgorithmException("no checksum algorithm given");
        }

        LOGGER.debug("verify file {} by checksum algorithm {}", file.getName(), checksumAlgorithm);

        // get expected checksum and checksum of given file
        String expectedChecksum = readExpectedChecksum(file, checksumAlgorithm);
        String fileChecksum = calculateChecksum(file, checksumAlgorithm);

        LOGGER.debug("expected checksum of file '{}':   {}", file.getName(), expectedChecksum);
        LOGGER.debug("calculated checksum of file '{}': {}", file.getName(), fileChecksum);
        return fileChecksum.equals(expectedChecksum);
    }

    /**
     * read the expected checksum from checksum text file (file extension is given by checksum algorithm, e.g. .md5)
     */
    private static String readExpectedChecksum(File file, ChecksumAlgorithm checksumAlgorithm) throws FileNotFoundException {
        File checksumFile = new File(file.getAbsolutePath() + "." + checksumAlgorithm.getExtensions());
        try {
            return new String(Files.readAllBytes(Paths.get(checksumFile.getAbsolutePath())));
        } catch (Exception e) {
            throw new FileNotFoundException("can't read checksum from checksum file " + checksumFile.getAbsolutePath());
        }
    }
    
    /**
     * calculate checksum of given file by given algorithm
     */
    private static String calculateChecksum(File file, ChecksumAlgorithm checksumAlgorithm) throws NoSuchAlgorithmException, IOException {
        MessageDigest crypt = MessageDigest.getInstance(checksumAlgorithm.toString());
        byte[] fileData = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

        crypt.reset();
        crypt.update(fileData);
        return byteToHex(crypt.digest());
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
