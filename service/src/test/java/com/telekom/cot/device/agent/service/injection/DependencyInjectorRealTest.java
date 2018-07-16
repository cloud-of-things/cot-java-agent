package com.telekom.cot.device.agent.service.injection;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManagerImpl;
import com.telekom.cot.device.agent.common.configuration.Configuration;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManagerImpl;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.cot.device.agent.service.AgentServiceProvider;

public class DependencyInjectorRealTest {

    private DependencyInjector dependencyInjector;
    private List<AgentService> services;
    private TestServiceOne serviceOne = new TestServiceOne();
    private TestServiceTwo serviceTwo = new TestServiceTwo();
    private TestServiceThree serviceThree = new TestServiceThree();
    private TestServiceFour serviceFour = new TestServiceFour();
    private TestServiceFive testServiceFive = new TestServiceFive();
    private TestServiceSix testServiceSix = new TestServiceSix();

    @Before
    public void setUp() throws URISyntaxException, AbstractAgentException {
        AgentServiceProvider serviceProvider = Mockito.mock(AgentServiceProvider.class);
        // configurationManager
        Path configurationFile = Paths.get(DependencyInjectorMockTest.class.getResource("/readtest.yaml").toURI());
        ConfigurationManager configurationManager = ConfigurationManagerImpl.getInstance(configurationFile);
        // credentialsManager
        Path deviceCredentialsFile = Paths.get(DependencyInjectorMockTest.class.getResource("/test-credentials.yaml").toURI());
        AgentCredentialsManager credentialsManager = AgentCredentialsManagerImpl.getInstance(deviceCredentialsFile);
        this.dependencyInjector = DependencyInjector
                        .getInstance(serviceProvider, configurationManager, credentialsManager);
        this.services = new ArrayList<>();
        services.add(serviceOne);
        services.add(serviceTwo);
        services.add(serviceThree);
        services.add(serviceFour);
        services.add(testServiceFive);
        services.add(testServiceSix);
    }

    @Test
    public void testInjectConfigurations() throws AbstractAgentException {
        dependencyInjector.injectConfigurations(services);
        assertNotNull(serviceOne.getTestConf());
    }

    @Test
    public void testInjectAgentServices() throws AbstractAgentException {
        dependencyInjector.injectAgentServices(services);
        assertNotNull(serviceOne.getServiceFour());
        assertNotNull(serviceOne.getServiceFive());
        assertNotNull(serviceOne.getServiceSix());
        assertNull(serviceOne.getServiceSeven());
        assertNotNull(serviceTwo.getServiceFour());
        assertNotNull(serviceTwo.getServiceFive());
    }

    static class TestServiceOne extends TestServiceTwo {

        @Inject
        private TestServiceSix serviceSix;
        @Inject
        private TestServiceSeven serviceSeven;
        @Inject
        private TestConf testConf;

        public TestServiceSix getServiceSix() {
            return serviceSix;
        }

        public TestServiceSeven getServiceSeven() {
            return serviceSeven;
        }

        public TestConf getTestConf() {
            return testConf;
        }
    }

    static class TestServiceTwo extends TestServiceThree {

        @Inject
        private TestServiceFour serviceFour;

        public TestServiceFour getServiceFour() {
            return serviceFour;
        }
    }

    static class TestServiceThree extends AbstractAgentService {

        @Inject
        private TestServiceFive serviceFive;

        public TestServiceFive getServiceFive() {
            return serviceFive;
        }
    }

    static class TestServiceFour extends AbstractAgentService {
    }

    static class TestServiceFive extends AbstractAgentService {
    }

    static class TestServiceSix extends AbstractAgentService {
    }

    static class TestServiceSeven extends AbstractAgentService {
    }

    @ConfigurationPath("agent.platform")
    static class TestConf implements Configuration {

        private String textProperty;
        private Integer intProperty;
        private Double doubleProperty;
        private Boolean booleanProperty;

        public String getTextProperty() {
            return textProperty;
        }

        public void setTextProperty(String textProperty) {
            this.textProperty = textProperty;
        }

        public Integer getIntProperty() {
            return intProperty;
        }

        public void setIntProperty(Integer intProperty) {
            this.intProperty = intProperty;
        }

        public Double getDoubleProperty() {
            return doubleProperty;
        }

        public void setDoubleProperty(Double doubleProperty) {
            this.doubleProperty = doubleProperty;
        }

        public Boolean getBooleanProperty() {
            return booleanProperty;
        }

        public void setBooleanProperty(Boolean booleanProperty) {
            this.booleanProperty = booleanProperty;
        }
    }
}
