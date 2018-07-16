package com.telekom.cot.device.agent.common.injection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.configuration.Configuration;

public class InjectionUtilTest {

    @Mock
    private Logger mockLogger;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        InjectionUtil.injectStatic(InjectionUtil.class, mockLogger);
    }
    
    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<InjectionUtil> constructor = InjectionUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }
    
    @Test
    public void testGetFieldsNoClass() {
        List<?> result = InjectionUtil.getFields(null, Inject.class, Configuration.class);

        assertEquals(0, result.size());
        verify(mockLogger).error("no class type given");
    }
    
    @Test
    public void testGetFieldsNoAnnotation() {
        List<?> result = InjectionUtil.getFields(InjectFieldTestClass.class, null, Configuration.class);

        assertEquals(0, result.size());
        verify(mockLogger).error("no annotation type given");
    }
    
    @Test
    public void testGetFields() {
        List<?> result = InjectionUtil.getFields(InjectFieldTestClass.class, Inject.class, Configuration.class);
        assertEquals(2, result.size());
    }
    
    @Test
    public void testGetFieldsWithoutFieldType() {
        List<?> result = InjectionUtil.getFields(InjectFieldTestClass.class, Inject.class);
        assertEquals(4, result.size());
    }
    
    @Test
    public void testGetFieldByName(){
        Field field = InjectionUtil.getField(FieldTest.class, "field");
        assertEquals("field", field.getName());

        field = InjectionUtil.getField(FieldTest.class, "");
        assertNull(field);

        field = InjectionUtil.getField(FieldTest.class, "nonExisting");
        assertNull(field);

    }

    @Test
    public void testGetFieldNoClassType(){
        Field field = InjectionUtil.getField(null, String.class);
        
        assertNull(field);
        verify(mockLogger).error("no class type given");
    }
    
    @Test
    public void testGetFieldNoFieldType(){
        Field field = InjectionUtil.getField(FieldTest.class, (Class<?>)null);
        
        assertNull(field);
        verify(mockLogger).error("no field type given");
    }
    
    @Test
    public void testGetFieldByType(){
        Field field = InjectionUtil.getField(FieldTest.class, String.class);
        assertEquals("field", field.getName());

        field = InjectionUtil.getField(ChildClass.class, String.class);
        assertEquals("field", field.getName());

        field = InjectionUtil.getField(EmptyClass.class, String.class);
    }

    @Test
    public void testInject() {
        FieldTest fieldTest = new FieldTest();
        assertTrue(InjectionUtil.inject(fieldTest, "field", "newField"));
        assertEquals("newField", fieldTest.field);

        fieldTest = new FieldTest();
        assertTrue(InjectionUtil.inject(fieldTest, "newField"));
        assertEquals("newField", fieldTest.field);
    }

    @Test
    public void testInjectNull() {
        InjectFieldTestClass testClass = new InjectFieldTestClass();
        
        assertFalse(InjectionUtil.inject(null, "fieldString", "test"));
        verify(mockLogger).error("no class instance given to inject");

        reset(mockLogger);
        assertFalse(InjectionUtil.inject(null, "test"));
        verify(mockLogger).error("no class instance given to inject");

        reset(mockLogger);
        assertFalse(InjectionUtil.inject(testClass, null, "test"));
        verify(mockLogger).error("no field name given");

        reset(mockLogger);
        assertFalse(InjectionUtil.inject(testClass, null));
        verify(mockLogger).error("no object to inject given");
    }

    @Test
    public void testInjectException() {
        assertFalse(InjectionUtil.inject(new InjectFieldTestClass(), "fieldString", 3));
        verify(mockLogger).error(eq("can't inject object into field"), any(IllegalArgumentException.class));
    }
    
    @Test
    public void testStaticInject() {
        assertTrue(InjectionUtil.injectStatic(StaticFieldTest.class, "field", "newField"));
        assertEquals("newField", StaticFieldTest.field);

        assertTrue(InjectionUtil.injectStatic(StaticFieldTest.class, "newField"));
        assertEquals("newField", StaticFieldTest.field);
    }

    @Test
    public void testStaticNullInject() {
        assertFalse(InjectionUtil.injectStatic(null, null));
    }

    public static class InjectFieldTestClass {

        @Inject
        private String fieldString = "fieldString";
        @Inject
        private Integer fieldInteger = 0;
        @Inject
        private MyConfigurationA confA = new MyConfigurationA();
        @Inject
        private static MyConfigurationB confB = new MyConfigurationB();
        @SuppressWarnings("unused")
        private String withoutInject = "withoutInject";
    }
    
    static class MyConfigurationA implements Comparable<String>, Configuration {

        @Override
        public int compareTo(String o) {
            return 0;
        }
    }

    static class MyConfigurationB extends MyConfigurationA implements Cloneable {
        
    }
    
    public class FieldTest{
        String field = "fieldTest";
    }

    public class ChildClass extends FieldTest {}

    public class EmptyClass {}

    public static class StaticFieldTest {
        static String field = "fieldTest";
    }
}
