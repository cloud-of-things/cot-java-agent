package com.telekom.cot.device.agent.operation.handler;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.assertIsTrue;
import static com.telekom.cot.device.agent.common.util.AssertionUtil.assertNotNull;
import static com.telekom.cot.device.agent.common.util.AssertionUtil.createExceptionAndLog;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.cot.device.agent.common.exc.OperationServiceException;
import com.telekom.cot.device.agent.operation.handler.AgentOperationsHandlerService.OperationType;
import com.telekom.cot.device.agent.operation.handler.TestOperationExecute.GivenStatus;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.system.properties.Software;

public class OperationUtil {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationUtil.class);
    
    private static final String CANT_PERFORM_EXECUTE = "can't perform SoftwareList execute: ";

    /**
     * get the first entry of the c8y_SoftwareList object in a c8y_SoftwareList operation
     * @param operation
     */
    public static Software getSoftwareToUpdate(Operation operation) throws AbstractAgentException {
        // get c8y_SoftwareList array from operation
        JsonArray softwareListArray;
        try {
            softwareListArray = JsonArray.class.cast(operation.getProperty(OperationType.C8Y_SOFTWARELIST.getAttribute()));
        } catch (Exception e) {
            throw createExceptionAndLog(OperationServiceException.class, LOGGER,
                            CANT_PERFORM_EXECUTE + "can't get '" + OperationType.C8Y_SOFTWARELIST.getAttribute() + "' from operation", e);
        }
        
        // support only operations with only one c8y_SoftwareList entry
        assertIsTrue(softwareListArray.size() == 1, OperationServiceException.class, 
                        LOGGER, CANT_PERFORM_EXECUTE + "only operations containing one software list entry are supported");

        // try to map first entry in array to com.telekom.cot.device.agent.system.properties.Software
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(softwareListArray.get(0).toString());
            return mapper.treeToValue(node, Software.class);
        } catch (Exception e) {
            throw createExceptionAndLog(OperationServiceException.class, LOGGER,
                            CANT_PERFORM_EXECUTE + "can't get first software list entry", e); 
        }
    }
    
    /**
     * Get the given test operation status.
     * 
     * @param operation
     * @return GivenStatus
     * @throws AbstractAgentException
     */
    public static GivenStatus getGivenStatus(Operation operation) throws AbstractAgentException {
        // get testOperation
        @SuppressWarnings("unchecked")
        Map<String, Object> testOperation = operation.getProperty(OperationType.C8Y_TEST_OPERATION.getAttribute(), Map.class);
        assertNotNull(testOperation, AgentOperationHandlerException.class, LOGGER, "did not found the demo operation");
        // get status
        Object status = testOperation.get("givenStatus");
        assertNotNull(status, AgentOperationHandlerException.class, LOGGER, "did not found the demo operation status");
        // execute demo operation
        TestOperationExecute.GivenStatus givenStatus = TestOperationExecute.GivenStatus.find(status.toString());
        assertNotNull(givenStatus, AgentOperationHandlerException.class, LOGGER, "did not found the given operation status");
        LOGGER.info("execute demo operation by givenStatus={}", givenStatus);
        return givenStatus;
    }
}
