package com.telekom.cot.device.agent.platform.objects;

import java.util.Arrays;

public enum OperationStatus {
    
    /**
     * Operation is retrieved by device, accepted and now executing. Next status can be SUCCESSFULL or FAILED.
     */
    EXECUTING,

    /**
     * Operation execution failed or device denied execution.
     */
    FAILED,

    /**
     * Operation is new and awaits retrieval from device.
     */
    PENDING,

    /**
     * Operation is executed successfully.
     */
    SUCCESSFUL,

    /**
     * Just used in device registry and indicates an accepted device.
     */
    ACCEPTED,
    
    /**
     * Not available.
     */
    NA;

    public static OperationStatus findByName(final String name) {
        return Arrays.asList(OperationStatus.values()).stream()
                        // filter by name
                        .filter(os -> os.name().equals(name))
                        // get the first
                        .findFirst()
                        // get the result or NA
                        .orElse(NA);
    }
    
}
