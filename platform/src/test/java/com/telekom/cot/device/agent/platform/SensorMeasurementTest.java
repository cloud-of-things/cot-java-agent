package com.telekom.cot.device.agent.platform;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;


public class SensorMeasurementTest {
    //test SensorMeasurement
    private Date time;
    private static final String type = "testType";
    private static final float value = (float) 0.1;
    private static final String unit = "testUnit";
    
    SensorMeasurement testMeasurement = new SensorMeasurement(type, value, unit);

    @Before
    public void setUp()
    //set up the time/type/unit/value
    {
        testMeasurement.setTime(time);
        testMeasurement.setType(type);
        testMeasurement.setUnit(unit);
        testMeasurement.setValue(value);
    }
    @Test
    public void testGetterAndSetter() {
        //testing the getters and setters
        assertEquals(time, testMeasurement.getTime());
        assertEquals(type, testMeasurement.getType());
        assertEquals(value, testMeasurement.getValue(), 0.1);
        assertEquals(unit, testMeasurement.getUnit());
    }
}
