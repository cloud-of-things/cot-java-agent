package com.telekom.cot.device.agent.operation.operations;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.telekom.cot.device.agent.platform.objects.operation.Operation;
import com.telekom.cot.device.agent.platform.objects.operation.OperationAttributes;
import com.telekom.cot.device.agent.system.properties.Software;

@OperationAttributes(name = "c8y_SoftwareList")
public class SoftwareUpdateOperation extends Operation {
    
    /**
     * get information (name, version, download url) about the software to update
     * @return information about the software to update or {@code null}
     */
    public Software getSoftware() {
        // get c8y_SoftwareList array from operation
        // support only operations with only one c8y_SoftwareList entry
        JsonArray softwareListArray = getProperty(getOperationName(), JsonArray.class);
        if (Objects.isNull(softwareListArray) || softwareListArray.size() != 1) {
            return null;
        }
        
        // try to map first entry in array to com.telekom.cot.device.agent.system.properties.Software
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(softwareListArray.get(0).toString());
            return mapper.treeToValue(node, Software.class);
        } catch (Exception e) {
            return null;
        }
    }
}
