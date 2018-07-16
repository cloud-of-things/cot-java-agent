package com.telekom.cot.device.agent.common.util;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.junit.Assert;
import org.junit.Test;

public class ValidationUtilTest {

    @Test
    public void testValidObject() {
        TestClass testClass = new TestClass();
        testClass.setNotNull("Test");
        testClass.setPositive(1);

        Assert.assertEquals("Test", testClass.getNotNull());
        Assert.assertEquals(new Integer(1), testClass.getPositive());
        Assert.assertTrue(ValidationUtil.isValid(testClass));
    }

    @Test
    public void testInvalidObject() {
        TestClass testClass = new TestClass();
        Assert.assertFalse(ValidationUtil.isValid(testClass));

        testClass.setNotNull(null);
        Assert.assertFalse(ValidationUtil.isValid(testClass));

        testClass.setNotNull("Test");
        testClass.setPositive(-1);
        Assert.assertFalse(ValidationUtil.isValid(testClass));

        testClass = null;
        Assert.assertFalse(ValidationUtil.isValid(testClass));
    }

    @Test
    public void testValidList() {
        TestClass testClass = new TestClass();
        testClass.setNotNull("Test");
        testClass.setPositive(1);

        TestClass testClass2 = new TestClass();
        testClass2.setNotNull("Test2");
        testClass2.setPositive(2);

        List<TestClass> testClassList = new ArrayList<>();
        testClassList.add(testClass);
        testClassList.add(testClass2);

        Assert.assertTrue(ValidationUtil.isValid(testClassList));
    }

    @Test
    public void testPartiallyInvalidList() {
        TestClass testClass = new TestClass();
        testClass.setNotNull("Test");
        testClass.setPositive(1);

        TestClass testClass2 = new TestClass();
        testClass2.setPositive(-1);

        List<TestClass> testClassList = new ArrayList<>();
        testClassList.add(testClass);
        testClassList.add(testClass2);

        Assert.assertFalse(ValidationUtil.isValid(testClassList));
    }


    public class TestClass {
        @NotNull private String notNull;
        @Positive private Integer positive;

        public String getNotNull() {
            return notNull;
        }

        public void setNotNull(String notNull) {
            this.notNull = notNull;
        }

        public Integer getPositive() {
            return positive;
        }

        public void setPositive(Integer positive) {
            this.positive = positive;
        }
    }
}
