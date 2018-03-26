package com.telekom.cot.device.agent.common.util;

import java.io.File;
import java.nio.file.Path;

public class FileUtil {

    static boolean deleteDirectory(Path directoryToBeDeleted) {
        File directory = directoryToBeDeleted.toFile();
        File[] allFiles = directory.listFiles();
        if (allFiles != null) {
            for (File file : allFiles) {
                file.delete();
            }
        }

        return directory.delete();
    }
}
