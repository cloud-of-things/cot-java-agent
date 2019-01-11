package com.telekom.cot.device.agent.platform.objects.operation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.platform.objects.operation.Operation;
import com.telekom.cot.device.agent.platform.objects.operation.OperationAttributes;
import com.telekom.cot.device.agent.platform.objects.operation.OperationFactory;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;


public class OperationFactoryTest {

    private static final String OPERATION_ID = "12345";
    private static final HashMap<String, Object> EMPTY_FRAGMENT = new HashMap<String, Object>();
    private static final OperationStatus OPERATION_STATUS = OperationStatus.EXECUTING;
    private final static String OPERATION_1_NAME = "operation1";
    private final static String OPERATION_2_NAME = "operation2";
    
    private HashSet<Class<? extends Operation>> supportedOperationTypes;
    private HashMap<String, Object> operationProperties;
    private Operation abstractOperation;
    
    @Before
    public void setUp() {
        supportedOperationTypes = new HashSet<>();
        supportedOperationTypes.add(Operation1.class);
        supportedOperationTypes.add(Operation2.class);
    
        operationProperties = new HashMap<>();
        operationProperties.put("id", OPERATION_ID);
        operationProperties.put(OPERATION_1_NAME, EMPTY_FRAGMENT);
        operationProperties.put("status", OPERATION_STATUS);
        
        abstractOperation = new Operation(operationProperties) {};
    }
    
    @Test
    public void testGetOperationNameNull() {
        assertNull(OperationFactory.getOperationName(null));
    }
    
    @Test
    public void testGetOperationNameNotAnnotatedOperation() {
        assertNull(OperationFactory.getOperationName(UnnamedOperation.class));
    }
    
    @Test
    public void testGetOperationName() {
        assertEquals(OPERATION_1_NAME, OperationFactory.getOperationName(Operation1.class));
        assertEquals(OPERATION_2_NAME, OperationFactory.getOperationName(Operation2.class));
    }

    @Test
    public void testGetOperationNamesNull() {
        List<String> operationNames = OperationFactory.getOperationNames(null);
        assertNotNull(operationNames);
        assertEquals(0, operationNames.size());
    }
    
    @Test
    public void testGetOperationNamesEmptyOperationList() {
        List<String> operationNames = OperationFactory.getOperationNames(new ArrayList<Class<? extends Operation>>());
        assertNotNull(operationNames);
        assertEquals(0, operationNames.size());
    }

    @Test
    public void testGetOperationNamesNoAnnotatedOperation() {
        List<Class<? extends Operation>> operations = Arrays.asList(UnnamedOperation.class);
        List<String> operationNames = OperationFactory.getOperationNames(operations);
        assertNotNull(operationNames);
        assertEquals(0, operationNames.size());
    }

    @Test
    public void testGetOperationNames() {
        List<Class<? extends Operation>> operations = Arrays.asList(Operation2.class, UnnamedOperation.class, Operation1.class);
        List<String> operationNames = OperationFactory.getOperationNames(operations);
        assertNotNull(operationNames);
        assertEquals(2, operationNames.size());
        assertEquals(OPERATION_2_NAME, operationNames.get(0));
        assertEquals(OPERATION_1_NAME, operationNames.get(1));
    }

    @Test
    public void testCreateOperationNull() {
        assertNull(OperationFactory.createOperation(supportedOperationTypes, null));
    }
    
    @Test
    public void testCreateOperationNoSupportedOperations() {
        assertNull(OperationFactory.createOperation(null, operationProperties));
        assertNull(OperationFactory.createOperation(new HashSet<>(), operationProperties));
    }
    
    @Test
    public void testCreateOperationEmptyPropertiesMap() {
        assertNull(OperationFactory.createOperation(supportedOperationTypes, EMPTY_FRAGMENT));
    }
    
    @Test
    public void testCreateOperationNoOperationNameProperty() {
        operationProperties.remove(OPERATION_1_NAME);
        assertNull(OperationFactory.createOperation(supportedOperationTypes, operationProperties));
    }
    
    @Test
    public void testCreateOperationNotSupported() {
        supportedOperationTypes.remove(Operation1.class);
        assertNull(OperationFactory.createOperation(supportedOperationTypes, operationProperties));
    }
    
    @Test
    public void testCreateOperationConstructorExcpetion() {
        operationProperties.remove(OPERATION_1_NAME);
        operationProperties.put("exception_operation", EMPTY_FRAGMENT);
        supportedOperationTypes.add(ConstructorExceptionOperation.class);
        assertNull(OperationFactory.createOperation(supportedOperationTypes, operationProperties));
    }
    
    @Test
    public void testCreateOperationNoStatus() {
        operationProperties.remove("status");
        Operation operation = OperationFactory.createOperation(supportedOperationTypes, operationProperties);
        assertTrue(Operation1.class.isInstance(operation));
        assertEquals(OPERATION_ID, operation.getId());
        assertNull(operation.getStatus());
    }

    @Test
    public void testCreateOperationStatusString() {
        operationProperties.put("status", OPERATION_STATUS.name());
        Operation operation = OperationFactory.createOperation(supportedOperationTypes, operationProperties);
        assertTrue(Operation1.class.isInstance(operation));
        assertEquals(OPERATION_ID, operation.getId());
        assertEquals(OPERATION_STATUS, operation.getStatus());
    }

    @Test
    public void testCreateOperation() {
        Operation operation = OperationFactory.createOperation(supportedOperationTypes, operationProperties);
        assertTrue(Operation1.class.isInstance(operation));
        assertEquals(OPERATION_ID, operation.getId());
        assertEquals(OPERATION_STATUS, operation.getStatus());
    }

    @Test
    public void testConvertToSpecificOperationNoSupportedOperations() {
        assertNull(OperationFactory.convertToSpecificOperation(null, abstractOperation));
        assertNull(OperationFactory.convertToSpecificOperation(new HashSet<>(), abstractOperation));
    }
    
    @Test
    public void testConvertToSpecificOperationNoOperation() {
        assertNull(OperationFactory.convertToSpecificOperation(supportedOperationTypes, null));
    }
    
    @Test
    public void testConvertToSpecificOperation() {
        Operation operation = OperationFactory.convertToSpecificOperation(supportedOperationTypes, abstractOperation);
        assertNotNull(operation);
        assertTrue(Operation1.class.isInstance(operation));
        assertEquals(OPERATION_ID, operation.getId());
        assertEquals(OPERATION_STATUS, operation.getStatus());
    }
    
    public static class UnnamedOperation extends Operation {
    }
    
    @OperationAttributes(name=OPERATION_1_NAME)
    public static class Operation1 extends Operation {
    }
    
    @OperationAttributes(name=OPERATION_2_NAME)
    public static class Operation2 extends Operation {
    }
    
    @OperationAttributes(name="exception_operation")
    public static class ConstructorExceptionOperation extends Operation {
        
        ConstructorExceptionOperation() {
            throw new RuntimeException();
        }
    }
}
