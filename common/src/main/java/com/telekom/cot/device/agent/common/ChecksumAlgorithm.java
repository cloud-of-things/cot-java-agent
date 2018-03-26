package com.telekom.cot.device.agent.common;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChecksumAlgorithm {
    
    MD5("MD5", "md5"), SHA1("SHA-1", "sha1"), SHA256("SHA-256", "sha256");

    private String extensions;
    private String algorithm;

    private ChecksumAlgorithm(String algorithm, String extensions) {
        this.extensions = extensions;
        this.algorithm = algorithm;
    }

    public String getExtensions() {
        return extensions;
    }

    @JsonValue
    @Override
    public String toString() {
        return algorithm;
    }

    @JsonCreator
    public static ChecksumAlgorithm fromValue(String value) {
        return StringUtils.isNotEmpty(value) ? ChecksumAlgorithm.valueOf(value.toUpperCase()) : MD5;
    }
}
