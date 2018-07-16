package com.telekom.cot.device.agent.demo.sensor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.sensor.configuration.SensorConfiguration;

@ConfigurationPath("agent.demo.sensors.demoTemperatureSensor")
public class DemoTemperatureSensorConfiguration extends SensorConfiguration {

    @NotNull @NotEmpty
    private String valueFilePath;

    private boolean repeatMeasurements;
    
    public String getValueFilePath() {
        return valueFilePath;
    }

    public void setValueFilePath(String valueFilePath) {
        this.valueFilePath = valueFilePath;
    }

    public boolean isRepeatMeasurements() {
        return repeatMeasurements;
    }

    public void setRepeatMeasurements(boolean repeatMeasurements) {
        this.repeatMeasurements = repeatMeasurements;
    }
}
