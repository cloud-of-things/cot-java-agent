package com.telekom.cot.device.agent.demo.sensor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemperatureFileReader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TemperatureFileReader.class);
	
	private List<Float> temperatures = new ArrayList<>();
	private int index;
	private boolean repeatMeasurements = true;
	private boolean stopSendingMeasurements = false;

	/**
	 * tells whether to repeat the read measurements
	 */
    public boolean isRepeatMeasurements() {
        return repeatMeasurements;
    }

    /**
     * sets whether to repeat the read measurements
     */
    public void setRepeatMeasurements(boolean repeatMeasurements) {
        this.repeatMeasurements = repeatMeasurements;
    }

    /**
	 * reads temperature values (float values without unit) from a text file, one value per line
	 * @param filepath path of the file to read
	 * @return whether reading the file has been successful (minimum one temperature value)
	 */
	public boolean readFile(String filepath) {
		// clear existing measurements
		temperatures.clear();
		
		// read text lines from file and get measurements from text lines
		List<String> textLines = readAllLinesFromFile(filepath);
		return getTemperatureMeassurements(textLines);
	}
	
	/**
	 * gets the next temperature value
	 * @return the next temperature value or {@code null} if there are no (more) values
	 */
	public Float getTemperatureMeasurement() {
	    // check whether measurements are available
		if (stopSendingMeasurements) {
			return null;
		}
		
		// check whether to repeat measurements
	    stopSendingMeasurements = index == (temperatures.size() - 1) && !repeatMeasurements;
		
		// get and return next temperature
		Float value = temperatures.get(index);
        index = (index + 1) % temperatures.size();
		return value;
	}
	
	/**
	 * reads all text lines from file at given file path
	 */
	private List<String> readAllLinesFromFile(String filepath) {
		try {
			return Files.readAllLines(Paths.get(filepath));
		} catch(Exception e) {
			LOGGER.error("Can't read text in file '{}'", filepath, e);
			return new ArrayList<>();
		}
	}
	
	/**
	 * parses the given text lines as float values and collects them as SensorMeasurementReading
	 */
	private boolean getTemperatureMeassurements(List<String> textLines) {
		// clear existing measurements
		temperatures.clear();
		index = 0;
		
		for(String textLine : textLines) {
			try {
				temperatures.add(Float.valueOf(textLine));
			} catch(Exception e) {
				LOGGER.warn("Can't parse text line '{}' as temperature value (float number)", textLine, e);
			}
		}
		stopSendingMeasurements = temperatures.isEmpty();
		return !temperatures.isEmpty();
	}
}
