package com.telekom.cot.device.agent.platform.objects.operation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationFactory.class);
    
    private OperationFactory() {
    }
    
    /**
     * get the operation name for an annotated (by {@link OperationAttribute}) operation type 
     * @param operationType operation type to get the name of
     * @return the name of the operation or {@code null}
     */
    public static String getOperationName(Class<? extends Operation> operationType) {
        if (Objects.isNull(operationType)) {
            return null;
        }
        
        OperationAttributes operationAttributes = operationType.getAnnotation(OperationAttributes.class);
        return Objects.nonNull(operationAttributes) ? operationAttributes.name() : null;
    }

    /**
     * get the operation names for all annotated (by {@link OperationAttribute}) operation types 
     * @param operationTypes list of operation types to get the name of
     * @return list of names of the given operation types (maybe empty)
     */
    public static List<String> getOperationNames(List<Class<? extends Operation>> operationTypes) {
        ArrayList<String> operationNames = new ArrayList<>(); 
        if (Objects.nonNull(operationTypes)) {
            operationTypes.stream().forEach(
                            operationType -> {
                                String operationName = getOperationName(operationType);
                                if (StringUtils.isNotEmpty(operationName)) {
                                    operationNames.add(operationName);
                                }
                            });
        }
        
        return operationNames;
    }
    
    /**
     * create an operation instance by the given representation map<br/>
     * @param supportedOperations a set of supported operation types to create an operation of a specific type
     * @param operationRepresentation the (JSON) representation of the operation to create
     * @return a created operation instance or {@code null}
     */
    public static Operation createOperation(Set<Class<? extends Operation>> supportedOperations, Map<String, Object> operationRepresentation) {
        if (Objects.isNull(supportedOperations) || supportedOperations.isEmpty()) {
            LOGGER.error("no supported operations given");
            return null;
        }
        
        if (Objects.isNull(operationRepresentation) || operationRepresentation.isEmpty()) {
            LOGGER.error("no operation representation map given");
            return null;
        }
        
        Class<? extends Operation> operationType = getOperationType(supportedOperations, operationRepresentation.keySet());
        if(Objects.isNull(operationType)) {
            LOGGER.error("can't get type of operation (maybe not supported?");
            return null;
        }

        return createOperation(operationType, operationRepresentation);
    }
    
    /**
     * convert a not specific abstract operation (of type Operation) to a specific operation (e.g. of type TestOperation, RestartOperation...)
     * @param supportedOperations a set of supported operation types to create an operation of a specific type
     * @param operationToConvert the not specific operation to create
     * @return an instance of a specific operation type or {@code null} if converting is not possible
     */
    public static Operation convertToSpecificOperation(Set<Class<? extends Operation>> supportedOperations, Operation operationToConvert) {
        if (Objects.isNull(operationToConvert)) {
            LOGGER.error("no operation to convert given");
            return null;
        }
        
        return createOperation(supportedOperations, operationToConvert.getProperties());
    }

    /**
     * converts the given operation into the given specific operation type
     * @param operationType type of operation to convert to
     * @param operation operation to convert
     * @return a converted operation of given type or {@code null}
     */
    public static <T extends Operation> T convertTo(Class<T> operationType, Operation operation) {
        if(Objects.isNull(operationType)) {
            LOGGER.error("no operation type for convertion given");
            return null;
        }

        HashSet<Class<? extends Operation>> supportedOperations = new HashSet<>();
        supportedOperations.add(operationType);
        
        Operation convertedOperation = convertToSpecificOperation(supportedOperations, operation);
        return operationType.isInstance(convertedOperation) ? operationType.cast(convertedOperation) : null;
    }
    
    /**
     * converts the given list of operations into the given specific operation type
     * @param operationType type of operation to convert to
     * @param operations list of operations to convert
     * @return a list of converted operations of given type, maybe empty
     */
    @SuppressWarnings("unchecked")
    public static <T extends Operation> List<T> convertTo(Class<T> operationType, List<Operation> operations) {
        ArrayList<T> convertedOperations = new ArrayList<>();
        
        if(Objects.isNull(operationType)) {
            LOGGER.error("no operation type for convertion given");
            return convertedOperations;
        }

        if(Objects.isNull(operations)) {
            LOGGER.error("no operations to convert given");
            return convertedOperations;
        }

        // return list if type is Operation.class
        if(Operation.class.equals(operationType)) {
            return (List<T>) operations;
        }
        
        operations.stream().forEach(operation -> {
            T resultOperation = OperationFactory.convertTo(operationType, operation);
            if(Objects.nonNull(resultOperation)) {
                convertedOperations.add(resultOperation); 
            }
        });
        
        return convertedOperations;
    }
    
    /**
     * search for an operation type in the supported operations list by the given set of property names (from JSON map)
     */
    private static Class<? extends Operation> getOperationType(Set<Class<? extends Operation>> supportedOperations, Set<String> operationProperties) {
        Optional<Class<? extends Operation>> optionalOperationType = supportedOperations.stream().filter(
                        type -> operationProperties.contains(getOperationName(type))).findFirst();
        
        return optionalOperationType.orElse(null);
    }
    
    /**
     * create an operation instance by the given type an the given representation map
     */
    private static Operation createOperation(Class<? extends Operation> operationType, Map<String, Object> operationRepresentation) {
        try {
            // get default constructor and create new instance, set properties map
            Operation operation = operationType.getConstructor().newInstance();
            operation.setProperties(operationRepresentation);
            LOGGER.info("created operation of type '{}' successfully", operationType);
            
            // set status
            Object status = operationRepresentation.get("status");
            if (Objects.isNull(status) || Operation.OperationStatus.class.isInstance(status)) {
                return operation;
            }
            operation.setStatus(Operation.OperationStatus.valueOf((String)status));
            return operation;
        } catch (Exception e) {
            LOGGER.error("can't create operation of type '{}'", operationType, e);
            return null;
        }
    }
}
