package com.telekom.cot.device.agent.common.util;

import static org.junit.Assert.*;

import org.junit.Test;

import java.lang.reflect.Field;

public class InjectionUtilTest {

    @Test
    public void getFieldByStringTest(){
        Field field = InjectionUtil.getField(FieldTest.class, "field");
        assertEquals("field", field.getName());

        field = InjectionUtil.getField(FieldTest.class, "");
        assertNull(field);

        field = InjectionUtil.getField(FieldTest.class, "nonExisting");
        assertNull(field);

    }

    @Test
    public void getFieldByTypeTest(){
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
        assertFalse(InjectionUtil.inject(null, null, null));
        assertFalse(InjectionUtil.inject(null, null));
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


    public class FieldTest{
        String field = "fieldTest";
    }

    public class ChildClass extends FieldTest {}

    public class EmptyClass {}

    public static class StaticFieldTest {
        static String field = "fieldTest";
    }

}
