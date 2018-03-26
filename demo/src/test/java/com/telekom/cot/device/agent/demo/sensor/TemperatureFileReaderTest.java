package com.telekom.cot.device.agent.demo.sensor;

import static org.junit.Assert.*;

import org.junit.Test;

public class TemperatureFileReaderTest {

    private TemperatureFileReader temperatureFileReader = new TemperatureFileReader();

    /**
     * test method TemperatureFileReader.readFile without file path (null)
     */
    @Test
    public void testReadFileNoPath() {
        assertFalse(temperatureFileReader.readFile(null));
    }

    /**
     * test method TemperatureFileReader.readFile without empty file path
     */
    @Test
    public void testReadFileEmptyPath() {
        assertFalse(temperatureFileReader.readFile(""));
    }

    /**
     * test method TemperatureFileReader.readFile with not existing file
     */
    @Test
    public void testReadFileNotExistingFile() {
        assertFalse(temperatureFileReader.readFile("notExistingFile.txt"));
    }

    /**
     * test method TemperatureFileReader.readFile with an empty file
     */
    @Test
    public void testReadFileEmptyFile() {
        assertFalse(temperatureFileReader.readFile("src/test/resources/emptyValueFile.txt"));
    }

    /**
     * test method TemperatureFileReader.readFile with a file with invalid values
     */
    @Test
    public void testReadFileInvalidValues() {
        assertTrue(temperatureFileReader.readFile("src/test/resources/invalidValueFile.txt"));
    }

    /**
     * test method TemperatureFileReader.readFile
     */
    @Test
    public void testReadFile() {
        assertTrue(temperatureFileReader.readFile("src/test/resources/temperatures.txt"));
    }

    /**
     * test method TemperatureFileReader.getTemperatureMeasurement without calling TemperatureFileReader.readFile before
     */
    public void testGetTemperatureMeasurementNoReadFile() {
        assertNull(temperatureFileReader.getTemperatureMeasurement());
    }

    /**
     * test method TemperatureFileReader.getTemperatureMeasurement with not existing file
     */
    @Test
    public void testGetTemperatureMeasurementNotExistingFile() {
        temperatureFileReader.readFile("notExistingFile.txt");
        assertNull(temperatureFileReader.getTemperatureMeasurement());
    }

    /**
     * test method TemperatureFileReader.getTemperatureMeasurement with an empty file
     */
    @Test
    public void testGetTemperatureMeasurementEmptyFile() {
        temperatureFileReader.readFile("src/test/resources/emptyValueFile.txt");
        assertNull(temperatureFileReader.getTemperatureMeasurement());
    }

    /**
     * test method TemperatureFileReader.getTemperatureMeasurement with a file with invalid values
     */
    @Test
    public void testGetTemperatureMeasurementInvalidValues() {
        temperatureFileReader.readFile("src/test/resources/invalidValueFile.txt");
        for (int times = 1; times <= 3; times++) {
            assertEquals(19.7f, temperatureFileReader.getTemperatureMeasurement(), 0.0001f);
            assertEquals(25.3f, temperatureFileReader.getTemperatureMeasurement(), 0.0001f);
            assertEquals(18.0f, temperatureFileReader.getTemperatureMeasurement(), 0.0001f);
        }
    }

    /**
     * test method TemperatureFileReader.getTemperatureMeasurement
     */
    @Test
    public void testGetTemperatureMeasurement() {
        temperatureFileReader.readFile("src/test/resources/temperatures.txt");
        for (int times = 1; times <= 3; times++) {
            assertEquals(21.7f, temperatureFileReader.getTemperatureMeasurement(), 0.0001f);
            assertEquals(23.4f, temperatureFileReader.getTemperatureMeasurement(), 0.0001f);
            assertEquals(22.8f, temperatureFileReader.getTemperatureMeasurement(), 0.0001f);
            assertEquals(24.9f, temperatureFileReader.getTemperatureMeasurement(), 0.0001f);
            assertEquals(28.5f, temperatureFileReader.getTemperatureMeasurement(), 0.0001f);
        }
    }

    /**
     * test method TemperatureFileReader.getTemperatureMeasurement
     */
    @Test
    public void testGetTemperatureMeasurementRepeatMeasurementsFalse() {
        temperatureFileReader.readFile("src/test/resources/temperatures.txt");
        temperatureFileReader.setRepeatMeasurements(false);
        assertEquals(21.7f, temperatureFileReader.getTemperatureMeasurement(), 0.0001f);
        assertEquals(23.4f, temperatureFileReader.getTemperatureMeasurement(), 0.0001f);
        assertEquals(22.8f, temperatureFileReader.getTemperatureMeasurement(), 0.0001f);
        assertEquals(24.9f, temperatureFileReader.getTemperatureMeasurement(), 0.0001f);
        assertEquals(28.5f, temperatureFileReader.getTemperatureMeasurement(), 0.0001f);
        assertNull(temperatureFileReader.getTemperatureMeasurement());
    }
}
