package com.telekom.cot.device.agent.service.injection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.configuration.Configuration;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.ConfigurationNotFoundException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.event.AgentContext;
import com.telekom.cot.device.agent.service.injection.DependencyInjector;

@RunWith(PowerMockRunner.class)
public class DependencyInjectorMockTest {
    
    private static final String NO_ANNOTATED_FIELDS = "can't find '@Inject' annotated fields of type '{}' at service '{}'";

    private static final String INJECT_CONFIGURATIONS_INTO_SERVICE = "inject configurations into service '{}'";
    private static final String INJECT_CONFIGURATION = "inject configuration '{}' into service '{}', member field '{}'";
    private static final String INJECTED_CONFIGURATION_SUCCESSFULLY = "injected configurations into service '{}' successfully";
    private static final String INJECT_CONFIGURATIONS_SERVICE_SERVICE_NOT_ADDED = "service '{}' not added, can't inject configurations";

    private static final String INJECT_CONFIGURATION_MANAGER_INTO_SERVICE = "inject configuration manager into service '{}'";
    private static final String INJECT_CONFIGURATION_MANAGER = "inject configuration manager into service '{}', member field '{}'";
    private static final String INJECT_CONFIGURATION_MANAGER_SERVICE_NOT_ADDED = "service '{}' not added, can't inject configuration manager";
    private static final String INJECTED_CONFIGURATION_MANAGER_SUCCESSFULLY = "injected configuration manager into service '{}' successfully";
    
    private static final String INJECT_AGENT_SERVICES_INTO_SERVICE = "inject agent services into service '{}'";
    private static final String INJECT_AGENT_SERVICES = "inject agent service '{}' into service '{}', member field '{}'";
    private static final String INJECT_AGENT_SERVICES_SERVICE_NOT_ADDED = "service '{}' not added, can't inject agent services";
    private static final String INJECTED_AGENT_SERVICES_SUCCESSFULLY = "injected agent services into service '{}' successfully";

    private static final String INJECT_AGENT_SERVICE_PROVIDER_INTO_SERVICE = "inject agent service provider into service '{}'";
    private static final String INJECT_AGENT_SERVICE_PROVIDER = "inject agent service provider into service '{}', member field '{}'";
    private static final String INJECT_AGENT_SERVICE_PROVIDER_SERVICE_NOT_ADDED = "service '{}' not added, can't inject agent service provider";
    private static final String INJECTED_AGENT_SERVICE_PROVIDER_SUCCESSFULLY = "injected agent service provider into service '{}' successfully";
    
    private static final String INJECT_CREDENTIALS_MANAGER_INTO_SERVICE = "inject agent credentials manager into service '{}'";
    private static final String INJECT_CREDENTIALS_MANAGER = "inject agent credentials manager into service '{}', member field '{}'";
    private static final String INJECT_CREDENTIALS_MANAGER_SERVICE_NOT_ADDED = "service '{}' not added, can't inject agent credentials manager";
    private static final String INJECTED_CREDENTIALS_MANAGER_SUCCESSFULLY = "injected agent credentials manager into service '{}' successfully";
    
    @Mock private Logger mockLogger;
    @Mock private AgentServiceProvider mockServiceProvider;
    @Mock private ConfigurationManager mockConfigurationManager;
    @Mock private AgentCredentialsManager mockCredentialsManager;
    
    private TestConfiguration configuration;
    private TestServiceNoFields testServiceNoFields;
    private TestServiceNoAnnotatedFields testServiceNoAnnotatedFields; 
    private TestService testService; 
    private ArrayList<AgentService> services = new ArrayList<>();
    
    private DependencyInjector dependencyInjector;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        dependencyInjector = DependencyInjector.getInstance(mockServiceProvider, mockConfigurationManager, mockCredentialsManager);
        
        configuration = new TestConfiguration();
        testServiceNoFields = new TestServiceNoFields();
        testServiceNoAnnotatedFields = new TestServiceNoAnnotatedFields(); 
        testService = new TestService(); 

        services.clear();
        services.add(testServiceNoAnnotatedFields);
        services.add(testService);
        
        InjectionUtil.injectStatic(DependencyInjector.class, mockLogger);
        
        when(mockConfigurationManager.getConfiguration(TestConfiguration.class)).thenReturn(configuration);
    }
    
    // ********************************************************************************************************************************
    // DependencyInjector.injectConfigurations(AgentService)
    // ********************************************************************************************************************************
    
    /**
     * test DependencyInjector.injectConfigurations(AgentService),
     * no service given
     */
    @Test(expected=DependencyInjectorException.class)
    public void testInjectConfigurationsNoService() throws Exception {
        dependencyInjector.injectConfigurations((AgentService)null);
    }
    
    /**
     * test DependencyInjector.injectConfigurations(AgentService),
     * service without member fields given
     */
    @Test
    public void testInjectConfigurationsNoFields() throws Exception {
        dependencyInjector.injectConfigurations(testServiceNoFields);
        
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "Configuration", testServiceNoFields);
        verify(mockLogger, never()).debug(INJECT_CONFIGURATIONS_INTO_SERVICE, testServiceNoFields);
    }

    /**
     * test DependencyInjector.injectConfigurations(AgentService),
     * service with not annotated member fields given
     */
    @Test
    public void testInjectConfigurationsNoAnnotatedFields() throws Exception {
        dependencyInjector.injectConfigurations(testServiceNoAnnotatedFields);
        
        assertNull(testServiceNoAnnotatedFields.getConfiguration());
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "Configuration", testServiceNoAnnotatedFields);
        verify(mockLogger, never()).debug(INJECT_CONFIGURATIONS_INTO_SERVICE, testServiceNoAnnotatedFields);
    }

    /**
     * test DependencyInjector.injectConfigurations(AgentService),
     * ConfigurationManager.getConfiguration throws exception
     */
    @Test(expected=ConfigurationNotFoundException.class)
    public void testInjectConfigurationsGetConfigurationException() throws Exception {
        doThrow(new ConfigurationNotFoundException("not found")).when(mockConfigurationManager).getConfiguration(TestConfiguration.class);

        dependencyInjector.injectConfigurations(testService);
    }

    /**
     * test DependencyInjector.injectConfigurations(AgentService),
     * InjectionUtil.inject returns false
     */
    @Test(expected=DependencyInjectorException.class)
    @PrepareForTest(InjectionUtil.class)
    public void testInjectConfigurationsInjectionUtilFalse() throws Exception {
        List<Field> configurationFields = InjectionUtil.getFields(testService.getClass(), Inject.class, Configuration.class);
        
        PowerMockito.mockStatic(InjectionUtil.class);
        PowerMockito.when(InjectionUtil.getFields(TestService.class, Inject.class, Configuration.class)).thenReturn(configurationFields);
        PowerMockito.when(InjectionUtil.inject(testService, "configuration", configuration)).thenReturn(false);

        dependencyInjector.injectConfigurations(testService);
    }

    /**
     * test DependencyInjector.injectConfigurations(AgentService)
     */
    @Test
    public void testInjectConfigurations() throws Exception {
        dependencyInjector.injectConfigurations(testService);
        
        assertSame(configuration, testService.getConfiguration());
        verify(mockLogger, never()).debug(NO_ANNOTATED_FIELDS, "Configuration", testService);
        verify(mockLogger).debug(INJECT_CONFIGURATIONS_INTO_SERVICE, testService);
        verify(mockLogger).debug(INJECT_CONFIGURATION, configuration, testService, "configuration");
        verify(mockLogger).info(INJECTED_CONFIGURATION_SUCCESSFULLY, testService);
    }

    // ********************************************************************************************************************************
    // DependencyInjector.injectConfigurations(List<AgentService>)
    // ********************************************************************************************************************************
    
    /**
     * test DependencyInjector.injectConfigurations(List<AgentService>),
     * no service list given
     */
    @Test(expected=DependencyInjectorException.class)
    public void testInjectConfigurationsNoServiceList() throws Exception {
        dependencyInjector.injectConfigurations((List<AgentService>)null);
    }

    /**
     * test DependencyInjector.injectConfigurations(List<AgentService>),
     * service list is empty
     */
    @Test
    public void testInjectConfigurationsEmptyServiceList() throws Exception {
        services.clear();
        List<AgentService> result = dependencyInjector.injectConfigurations(services);
        
        assertTrue(result.isEmpty());
    }

    /**
     * test DependencyInjector.injectConfigurations(List<AgentService>),
     * service list contains only not annotated services
     */
    @Test
    public void testInjectConfigurationsServiceListNoAnnotatedFields() throws Exception {
        services.clear();
        services.add(testServiceNoAnnotatedFields);
        List<AgentService> result = dependencyInjector.injectConfigurations(services);
        
        assertEquals(1, result.size());
        assertSame(testServiceNoAnnotatedFields, result.get(0));
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "Configuration", testServiceNoAnnotatedFields);
        verify(mockLogger, never()).debug(INJECT_CONFIGURATIONS_INTO_SERVICE, testService);
    }

    /**
     * test DependencyInjector.injectConfigurations(List<AgentService>),
     * injectConfigurations throws exception
     */
    @Test
    public void testInjectConfigurationsServiceListException() throws Exception {
        AgentService nullService = null;
        services.clear();
        services.add(nullService);
        List<AgentService> result = dependencyInjector.injectConfigurations(services);
        
        assertEquals(0, result.size());
        verify(mockLogger).warn(INJECT_CONFIGURATIONS_SERVICE_SERVICE_NOT_ADDED, nullService);
    }

    /**
     * test DependencyInjector.injectConfigurations(List<AgentService>)
     */
    @Test
    public void testInjectConfigurationsServiceList() throws Exception {
        List<AgentService> result = dependencyInjector.injectConfigurations(services);
        
        assertEquals(2, result.size());
        assertSame(configuration, ((TestService)result.get(1)).getConfiguration());
        verify(mockLogger, never()).debug(NO_ANNOTATED_FIELDS, "Configuration", testService);
        verify(mockLogger).debug(INJECT_CONFIGURATIONS_INTO_SERVICE, testService);
        verify(mockLogger).debug(INJECT_CONFIGURATION, configuration, testService, "configuration");
        verify(mockLogger).info(INJECTED_CONFIGURATION_SUCCESSFULLY, testService);
    }

    
    // ********************************************************************************************************************************
    // DependencyInjector.injectConfigurationManager(AgentService)
    // ********************************************************************************************************************************
    
    /**
     * test DependencyInjector.injectConfigurationManager(AgentService),
     * no service given
     */
    @Test(expected=DependencyInjectorException.class)
    public void testInjectConfigurationManagerNoService() throws Exception {
        dependencyInjector.injectConfigurationManager((AgentService)null);
    }
    
    /**
     * test DependencyInjector.injectConfigurationManager(AgentService),
     * service without member fields given
     */
    @Test
    public void testInjectConfigurationManagerNoFields() throws Exception {
        dependencyInjector.injectConfigurationManager(testServiceNoFields);
        
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "ConfigurationManager", testServiceNoFields);
        verify(mockLogger, never()).debug(INJECT_CONFIGURATION_MANAGER_INTO_SERVICE, testServiceNoFields);
    }

    /**
     * test DependencyInjector.injectConfigurationManager(AgentService),
     * service with not annotated member fields given
     */
    @Test
    public void testInjectConfigurationManagerNoAnnotatedFields() throws Exception {
        dependencyInjector.injectConfigurationManager(testServiceNoAnnotatedFields);
        
        assertNull(testServiceNoAnnotatedFields.getConfigurationManager());
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "ConfigurationManager", testServiceNoAnnotatedFields);
        verify(mockLogger, never()).debug(INJECT_CONFIGURATION_MANAGER_INTO_SERVICE, testServiceNoAnnotatedFields);
    }

    /**
     * test DependencyInjector.injectConfigurationManager(AgentService),
     * InjectionUtil.inject returns false
     */
    @Test(expected=DependencyInjectorException.class)
    @PrepareForTest(InjectionUtil.class)
    public void testInjectConfigurationManagerInjectionUtilFalse() throws Exception {
        List<Field> annotatedConfigurationFields = InjectionUtil.getFields(testService.getClass(), Inject.class, ConfigurationManager.class);
        
        PowerMockito.mockStatic(InjectionUtil.class);
        PowerMockito.when(InjectionUtil.getFields(TestService.class, Inject.class, ConfigurationManager.class)).thenReturn(annotatedConfigurationFields);
        PowerMockito.when(InjectionUtil.inject(testService, "configurationManager", mockConfigurationManager)).thenReturn(false);

        dependencyInjector.injectConfigurationManager(testService);
    }

    /**
     * test DependencyInjector.injectConfigurationManager(AgentService)
     */
    @Test
    public void testInjectConfigurationManager() throws Exception {
        dependencyInjector.injectConfigurationManager(testService);
        
        assertSame(mockConfigurationManager, testService.getConfigurationManager());
        verify(mockLogger, never()).debug(NO_ANNOTATED_FIELDS, "ConfigurationManager", testService);
        verify(mockLogger).debug(INJECT_CONFIGURATION_MANAGER_INTO_SERVICE, testService);
        verify(mockLogger).debug(INJECT_CONFIGURATION_MANAGER, testService, "configurationManager");
        verify(mockLogger).info(INJECTED_CONFIGURATION_MANAGER_SUCCESSFULLY, testService);
    }


    // ********************************************************************************************************************************
    // DependencyInjector.injectConfigurationManager(List<AgentService>)
    // ********************************************************************************************************************************
    
    /**
     * test DependencyInjector.injectConfigurations(List<AgentService>),
     * no service list given
     */
    @Test(expected=DependencyInjectorException.class)
    public void testInjectConfigurationManagerNoServiceList() throws Exception {
        dependencyInjector.injectConfigurationManager((List<AgentService>)null);
    }

    /**
     * test DependencyInjector.injectConfigurationManager(List<AgentService>),
     * service list is empty
     */
    @Test
    public void testInjectConfigurationManagerEmptyServiceList() throws Exception {
        services.clear();
        List<AgentService> result = dependencyInjector.injectConfigurationManager(services);
        
        assertTrue(result.isEmpty());
    }

    /**
     * test DependencyInjector.injectConfigurationManager(List<AgentService>),
     * service list contains only not annotated services
     */
    @Test
    public void testInjectConfigurationManagerServiceListNoAnnotatedFields() throws Exception {
        services.clear();
        services.add(testServiceNoAnnotatedFields);
        List<AgentService> result = dependencyInjector.injectConfigurationManager(services);
        
        assertEquals(1, result.size());
        assertSame(testServiceNoAnnotatedFields, result.get(0));
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "ConfigurationManager", testServiceNoAnnotatedFields);
        verify(mockLogger, never()).debug(INJECT_CONFIGURATION_MANAGER_INTO_SERVICE, testServiceNoAnnotatedFields);
    }

    /**
     * test DependencyInjector.injectConfigurationManager(List<AgentService>),
     * injectConfigurationManager throws exception
     */
    @Test
    public void testInjectConfigurationManagerServiceListException() throws Exception {
        AgentService nullService = null;
        services.clear();
        services.add(nullService);
        List<AgentService> result = dependencyInjector.injectConfigurationManager(services);
        
        assertEquals(0, result.size());
        verify(mockLogger).warn(INJECT_CONFIGURATION_MANAGER_SERVICE_NOT_ADDED, nullService);
    }

    /**
     * test DependencyInjector.injectConfigurationManager(List<AgentService>)
     */
    @Test
    public void testInjectConfigurationManagerServiceList() throws Exception {
        List<AgentService> result = dependencyInjector.injectConfigurationManager(services);
        
        assertEquals(2, result.size());
        assertSame(mockConfigurationManager, ((TestService)result.get(1)).getConfigurationManager());
        verify(mockLogger, never()).debug(NO_ANNOTATED_FIELDS, "ConfigurationManager", testService);
        verify(mockLogger).debug(INJECT_CONFIGURATION_MANAGER_INTO_SERVICE, testService);
        verify(mockLogger).debug(INJECT_CONFIGURATION_MANAGER, testService, "configurationManager");
        verify(mockLogger).info(INJECTED_CONFIGURATION_MANAGER_SUCCESSFULLY, testService);
    }

    // ********************************************************************************************************************************
    // DependencyInjector.injectAgentServices(AgentService, List<AgentService>)
    // ********************************************************************************************************************************
    
    /**
     * test DependencyInjector.injectAgentServices(AgentService, List<AgentService>),
     * no service given
     */
    @Test(expected=DependencyInjectorException.class)
    public void testInjectAgentServicesNoService() throws Exception {
        dependencyInjector.injectAgentServices(null, services);
    }
    
    /**
     * test DependencyInjector.injectAgentServices(AgentService, List<AgentService>),
     * service without member fields given
     */
    @Test
    public void testInjectAgentServicesNoFields() throws Exception {
        dependencyInjector.injectAgentServices(testServiceNoFields, services);
        
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "AgentService", testServiceNoFields);
        verify(mockLogger, never()).debug(INJECT_AGENT_SERVICES_INTO_SERVICE, testServiceNoFields);
    }

    /**
     * test DependencyInjector.injectAgentServices(AgentService, List<AgentService>),
     * service with not annotated member fields given
     */
    @Test
    public void testInjectAgentServicesNoAnnotatedFields() throws Exception {
        dependencyInjector.injectAgentServices(testServiceNoAnnotatedFields, services);
        
        assertNull(testServiceNoAnnotatedFields.getTestServiceNoFields());
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "AgentService", testServiceNoAnnotatedFields);
        verify(mockLogger, never()).debug(INJECT_AGENT_SERVICES_INTO_SERVICE, testServiceNoAnnotatedFields);
    }

    /**
     * test DependencyInjector.injectAgentServices(AgentService, List<AgentService>),
     * AgentServiceProvider.getService throws exception
     */
    @Test(expected=DependencyInjectorException.class)
    public void testInjectAgentServicesGetServiceException() throws Exception {
        services.remove(0);
        
        dependencyInjector.injectAgentServices(testService, services);
    }

    /**
     * test DependencyInjector.injectConfigurations(AgentService),
     * InjectionUtil.inject returns false
     */
    @Test(expected=DependencyInjectorException.class)
    @PrepareForTest(InjectionUtil.class)
    public void testInjectAgentServicesInjectionUtilFalse() throws Exception {
        List<Field> serviceFields = InjectionUtil.getFields(testService.getClass(), Inject.class, AgentService.class);
        
        PowerMockito.mockStatic(InjectionUtil.class);
        PowerMockito.when(InjectionUtil.getFields(TestService.class, Inject.class, AgentService.class)).thenReturn(serviceFields);
        PowerMockito.when(InjectionUtil.inject(testService, "service", testServiceNoAnnotatedFields)).thenReturn(false);

        dependencyInjector.injectAgentServices(testService, services);
    }

    /**
     * test DependencyInjector.injectAgentServices(AgentService, List<AgentService>)
     */
    @Test
    public void testInjectAgentServices() throws Exception {
        dependencyInjector.injectAgentServices(testService, services);
        
        assertSame(testServiceNoAnnotatedFields, testService.getTestServiceNoAnnotatedFields());
        verify(mockLogger, never()).debug(NO_ANNOTATED_FIELDS, "AgentService", testService);
        verify(mockLogger).debug(INJECT_AGENT_SERVICES_INTO_SERVICE, testService);
        verify(mockLogger).debug(INJECT_AGENT_SERVICES, testServiceNoAnnotatedFields, testService, "service");
        verify(mockLogger).info(INJECTED_AGENT_SERVICES_SUCCESSFULLY, testService);
    }
    
    
    // ********************************************************************************************************************************
    // DependencyInjector.injectAgentServices(List<AgentService>)
    // ********************************************************************************************************************************
    
    /**
     * test DependencyInjector.injectAgentServices(List<AgentService>),
     * no service list given
     */
    @Test(expected=DependencyInjectorException.class)
    public void testInjectAgentServicesNoServiceList() throws Exception {
        dependencyInjector.injectAgentServices((List<AgentService>)null);
    }

    /**
     * test DependencyInjector.injectAgentServices(List<AgentService>),
     * service list is empty
     */
    @Test
    public void testInjectAgentServicesEmptyServiceList() throws Exception {
        services.clear();
        List<AgentService> result = dependencyInjector.injectAgentServices(services);
        
        assertTrue(result.isEmpty());
    }

    /**
     * test DependencyInjector.injectAgentServices(List<AgentService>),
     * service list contains only not annotated services
     */
    @Test
    public void testInjectAgentServicesServiceListNoAnnotatedFields() throws Exception {
        services.clear();
        services.add(testServiceNoAnnotatedFields);
        List<AgentService> result = dependencyInjector.injectAgentServices(services);
        
        assertEquals(1, result.size());
        assertSame(testServiceNoAnnotatedFields, result.get(0));
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "AgentService", testServiceNoAnnotatedFields);
        verify(mockLogger, never()).debug(INJECT_AGENT_SERVICES_INTO_SERVICE, testServiceNoAnnotatedFields);
    }

    /**
     * test DependencyInjector.injectAgentServices(List<AgentService>),
     * injectAgentServices throws exception
     */
    @Test
    public void testInjectAgentServicesServiceListException() throws Exception {
        AgentService nullService = null;
        services.clear();
        services.add(nullService);
        List<AgentService> result = dependencyInjector.injectAgentServices(services);
        
        assertEquals(0, result.size());
        verify(mockLogger).warn(INJECT_AGENT_SERVICES_SERVICE_NOT_ADDED, nullService);
    }

    /**
     * test DependencyInjector.injectAgentServices(List<AgentService>)
     */
    @Test
    public void testInjectAgentServicesServiceList() throws Exception {
        List<AgentService> result = dependencyInjector.injectAgentServices(services);
        
        assertEquals(2, result.size());
        assertSame(testServiceNoAnnotatedFields, ((TestService)result.get(1)).getTestServiceNoAnnotatedFields());
        verify(mockLogger, never()).debug(NO_ANNOTATED_FIELDS, "AgentService", testService);
        verify(mockLogger).debug(INJECT_AGENT_SERVICES_INTO_SERVICE, testService);
        verify(mockLogger).debug(INJECT_AGENT_SERVICES, testServiceNoAnnotatedFields, testService, "service");
        verify(mockLogger).info(INJECTED_AGENT_SERVICES_SUCCESSFULLY, testService);
    }

    
    // ********************************************************************************************************************************
    // DependencyInjector.injectAgentServiceProvider(AgentService)
    // ********************************************************************************************************************************
    
    /**
     * test DependencyInjector.injectAgentServiceProvider(AgentService),
     * no service given
     */
    @Test(expected=DependencyInjectorException.class)
    public void testInjectAgentServiceProviderNoService() throws Exception {
        dependencyInjector.injectAgentServiceProvider((AgentService)null);
    }
    
    /**
     * test DependencyInjector.injectAgentServiceProvider(AgentService),
     * service without member fields given
     */
    @Test
    public void testInjectAgentServiceProviderNoFields() throws Exception {
        dependencyInjector.injectAgentServiceProvider(testServiceNoFields);
        
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "AgentServiceProvider", testServiceNoFields);
        verify(mockLogger, never()).debug(INJECT_AGENT_SERVICE_PROVIDER_INTO_SERVICE, testServiceNoFields);
    }

    /**
     * test DependencyInjector.injectAgentServiceProvider(AgentService),
     * service with not annotated member fields given
     */
    @Test
    public void testInjectAgentServiceProviderNoAnnotatedFields() throws Exception {
        dependencyInjector.injectAgentServiceProvider(testServiceNoAnnotatedFields);
        
        assertNull(testServiceNoAnnotatedFields.getTestServiceNoFields());
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "AgentServiceProvider", testServiceNoAnnotatedFields);
        verify(mockLogger, never()).debug(INJECT_AGENT_SERVICE_PROVIDER_INTO_SERVICE, testServiceNoAnnotatedFields);
    }

    /**
     * test DependencyInjector.injectAgentServiceProvider(AgentService),
     * InjectionUtil.inject returns false
     */
    @Test(expected=DependencyInjectorException.class)
    @PrepareForTest(InjectionUtil.class)
    public void testInjectAgentServiceProviderInjectionUtilFalse() throws Exception {
        List<Field> serviceProviderFields = InjectionUtil.getFields(testService.getClass(), Inject.class, AgentServiceProvider.class);
        
        PowerMockito.mockStatic(InjectionUtil.class);
        PowerMockito.when(InjectionUtil.getFields(TestService.class, Inject.class, AgentServiceProvider.class)).thenReturn(serviceProviderFields);
        PowerMockito.when(InjectionUtil.inject(testService, "serviceProvider", testServiceNoAnnotatedFields)).thenReturn(false);

        dependencyInjector.injectAgentServiceProvider(testService);
    }

    /**
     * test DependencyInjector.injectAgentServiceProvider(AgentService)
     */
    @Test
    public void testInjectAgentServiceProvider() throws Exception {
        dependencyInjector.injectAgentServiceProvider(testService);
        
        assertSame(mockServiceProvider, testService.getAgentServiceProvider());
        verify(mockLogger, never()).debug(NO_ANNOTATED_FIELDS, "AgentServiceProvider", testService);
        verify(mockLogger).debug(INJECT_AGENT_SERVICE_PROVIDER_INTO_SERVICE, testService);
        verify(mockLogger).debug(INJECT_AGENT_SERVICE_PROVIDER, testService, "serviceProvider");
        verify(mockLogger).info(INJECTED_AGENT_SERVICE_PROVIDER_SUCCESSFULLY, testService);
    }
    
    
    // ********************************************************************************************************************************
    // DependencyInjector.injectAgentServiceProvider(List<AgentService>)
    // ********************************************************************************************************************************
    
    /**
     * test DependencyInjector.injectAgentServiceProvider(List<AgentService>),
     * no service list given
     */
    @Test(expected=DependencyInjectorException.class)
    public void testInjectAgentServiceProviderNoServiceList() throws Exception {
        dependencyInjector.injectAgentServiceProvider((List<AgentService>)null);
    }

    /**
     * test DependencyInjector.injectAgentServiceProvider(List<AgentService>),
     * service list is empty
     */
    @Test
    public void testInjectAgentServiceProviderEmptyServiceList() throws Exception {
        services.clear();
        List<AgentService> result = dependencyInjector.injectAgentServiceProvider(services);
        
        assertTrue(result.isEmpty());
    }

    /**
     * test DependencyInjector.injectAgentServiceProvider(List<AgentService>),
     * service list contains only not annotated services
     */
    @Test
    public void testInjectAgentServiceProviderServiceListNoAnnotatedFields() throws Exception {
        services.clear();
        services.add(testServiceNoAnnotatedFields);
        List<AgentService> result = dependencyInjector.injectAgentServiceProvider(services);
        
        assertEquals(1, result.size());
        assertSame(testServiceNoAnnotatedFields, result.get(0));
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "AgentServiceProvider", testServiceNoAnnotatedFields);
        verify(mockLogger, never()).debug(INJECT_AGENT_SERVICE_PROVIDER_INTO_SERVICE, testServiceNoAnnotatedFields);
    }

    /**
     * test DependencyInjector.injectAgentServiceProvider(List<AgentService>),
     * injectAgentServiceProvider throws exception
     */
    @Test
    public void testInjectAgentServiceProviderServiceListException() throws Exception {
        AgentService nullService = null;
        services.clear();
        services.add(nullService);
        List<AgentService> result = dependencyInjector.injectAgentServiceProvider(services);
        
        assertEquals(0, result.size());
        verify(mockLogger).warn(INJECT_AGENT_SERVICE_PROVIDER_SERVICE_NOT_ADDED, nullService);
    }

    /**
     * test DependencyInjector.injectAgentServiceProvider(List<AgentService>)
     */
    @Test
    public void testInjectAgentServiceProviderServiceList() throws Exception {
        List<AgentService> result = dependencyInjector.injectAgentServiceProvider(services);
        
        assertEquals(2, result.size());
        assertSame(mockServiceProvider, ((TestService)result.get(1)).getAgentServiceProvider());
        verify(mockLogger, never()).debug(NO_ANNOTATED_FIELDS, "AgentServiceProvider", testService);
        verify(mockLogger).debug(INJECT_AGENT_SERVICE_PROVIDER_INTO_SERVICE, testService);
        verify(mockLogger).debug(INJECT_AGENT_SERVICE_PROVIDER, testService, "serviceProvider");
        verify(mockLogger).info(INJECTED_AGENT_SERVICE_PROVIDER_SUCCESSFULLY, testService);
    }

    
    // ********************************************************************************************************************************
    // DependencyInjector.injectCredentialsManager(AgentService)
    // ********************************************************************************************************************************
    
    /**
     * test DependencyInjector.injectCredentialsManager(AgentService),
     * no service given
     */
    @Test(expected=DependencyInjectorException.class)
    public void testInjectCredentialsManagerNoService() throws Exception {
        dependencyInjector.injectCredentialsManager((AgentService)null);
    }
    
    /**
     * test DependencyInjector.injectCredentialsManager(AgentService),
     * service without member fields given
     */
    @Test
    public void testInjectCredentialsManagerNoFields() throws Exception {
        dependencyInjector.injectCredentialsManager(testServiceNoFields);
        
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "AgentCredentialsManager", testServiceNoFields);
        verify(mockLogger, never()).debug(INJECT_CREDENTIALS_MANAGER_INTO_SERVICE, testServiceNoFields);
    }

    /**
     * test DependencyInjector.injectCredentialsManager(AgentService),
     * service with not annotated member fields given
     */
    @Test
    public void testInjectCredentialsManagerNoAnnotatedFields() throws Exception {
        dependencyInjector.injectCredentialsManager(testServiceNoAnnotatedFields);
        
        assertNull(testServiceNoAnnotatedFields.getTestServiceNoFields());
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "AgentCredentialsManager", testServiceNoAnnotatedFields);
        verify(mockLogger, never()).debug(INJECT_CREDENTIALS_MANAGER_INTO_SERVICE, testServiceNoAnnotatedFields);
    }

    /**
     * test DependencyInjector.injectCredentialsManager(AgentService),
     * InjectionUtil.inject returns false
     */
    @Test(expected=DependencyInjectorException.class)
    @PrepareForTest(InjectionUtil.class)
    public void testInjectCredentialsManagerInjectionUtilFalse() throws Exception {
        List<Field> serviceProviderFields = InjectionUtil.getFields(testService.getClass(), Inject.class, AgentCredentialsManager.class);
        
        PowerMockito.mockStatic(InjectionUtil.class);
        PowerMockito.when(InjectionUtil.getFields(TestService.class, Inject.class, AgentCredentialsManager.class)).thenReturn(serviceProviderFields);
        PowerMockito.when(InjectionUtil.inject(testService, "credentialsManager", testServiceNoAnnotatedFields)).thenReturn(false);

        dependencyInjector.injectCredentialsManager(testService);
    }

    /**
     * test DependencyInjector.injectCredentialsManager(AgentService)
     */
    @Test
    public void testInjectCredentialsManager() throws Exception {
        dependencyInjector.injectCredentialsManager(testService);
        
        assertSame(mockCredentialsManager, testService.getCredentialsManager());
        verify(mockLogger, never()).debug(NO_ANNOTATED_FIELDS, "AgentCredentialsManager", testService);
        verify(mockLogger).debug(INJECT_CREDENTIALS_MANAGER_INTO_SERVICE, testService);
        verify(mockLogger).debug(INJECT_CREDENTIALS_MANAGER, testService, "credentialsManager");
        verify(mockLogger).info(INJECTED_CREDENTIALS_MANAGER_SUCCESSFULLY, testService);
    }
    
    
    // ********************************************************************************************************************************
    // DependencyInjector.injectCredentialsManager(List<AgentService>)
    // ********************************************************************************************************************************
    
    /**
     * test DependencyInjector.injectCredentialsManager(List<AgentService>),
     * no service list given
     */
    @Test(expected=DependencyInjectorException.class)
    public void testInjectCredentialsManagerNoServiceList() throws Exception {
        dependencyInjector.injectCredentialsManager((List<AgentService>)null);
    }

    /**
     * test DependencyInjector.injectCredentialsManager(List<AgentService>),
     * service list is empty
     */
    @Test
    public void testInjectCredentialsManagerEmptyServiceList() throws Exception {
        services.clear();
        List<AgentService> result = dependencyInjector.injectCredentialsManager(services);
        
        assertTrue(result.isEmpty());
    }

    /**
     * test DependencyInjector.injectCredentialsManager(List<AgentService>),
     * service list contains only not annotated services
     */
    @Test
    public void testInjectCredentialsManagerServiceListNoAnnotatedFields() throws Exception {
        services.clear();
        services.add(testServiceNoAnnotatedFields);
        List<AgentService> result = dependencyInjector.injectCredentialsManager(services);
        
        assertEquals(1, result.size());
        assertSame(testServiceNoAnnotatedFields, result.get(0));
        verify(mockLogger).debug(NO_ANNOTATED_FIELDS, "AgentCredentialsManager", testServiceNoAnnotatedFields);
        verify(mockLogger, never()).debug(INJECT_CREDENTIALS_MANAGER_INTO_SERVICE, testServiceNoAnnotatedFields);
    }

    /**
     * test DependencyInjector.injectCredentialsManager(List<AgentService>),
     * injectAgentCredentialsManager throws exception
     */
    @Test
    public void testInjectCredentialsManagerServiceListException() throws Exception {
        AgentService nullService = null;
        services.clear();
        services.add(nullService);
        List<AgentService> result = dependencyInjector.injectCredentialsManager(services);
        
        assertEquals(0, result.size());
        verify(mockLogger).warn(INJECT_CREDENTIALS_MANAGER_SERVICE_NOT_ADDED, nullService);
    }

    /**
     * test DependencyInjector.injectCredentialsManager(List<AgentService>)
     */
    @Test
    public void testInjectCredentialsManagerServiceList() throws Exception {
        List<AgentService> result = dependencyInjector.injectCredentialsManager(services);
        
        assertEquals(2, result.size());
        assertSame(mockCredentialsManager, ((TestService)result.get(1)).getCredentialsManager());
        verify(mockLogger, never()).debug(NO_ANNOTATED_FIELDS, "AgentCredentialsManager", testService);
        verify(mockLogger).debug(INJECT_CREDENTIALS_MANAGER_INTO_SERVICE, testService);
        verify(mockLogger).debug(INJECT_CREDENTIALS_MANAGER, testService, "credentialsManager");
        verify(mockLogger).info(INJECTED_CREDENTIALS_MANAGER_SUCCESSFULLY, testService);
    }

    
    // ********************************************************************************************************************************
    // classes used for tests
    // ********************************************************************************************************************************
    
    /**
     * test service
     */
    class TestService extends TestServiceNoFields {
        
        @Inject private TestConfiguration configuration = null;
        @Inject private ConfigurationManager configurationManager = null;
        @Inject private TestServiceNoAnnotatedFields service = null;
        @Inject private AgentServiceProvider serviceProvider = null;
        @Inject private AgentCredentialsManager credentialsManager = null;
        
        public TestConfiguration getConfiguration() {
            return configuration;
        }

        public ConfigurationManager getConfigurationManager() {
            return configurationManager;
        }

        public TestServiceNoAnnotatedFields getTestServiceNoAnnotatedFields() {
            return service;
        }

        public AgentServiceProvider getAgentServiceProvider() {
            return serviceProvider;
        }

        public AgentCredentialsManager getCredentialsManager() {
            return credentialsManager;
        }
    }
    
    /**
     * test service with no annotated fields
     */
    class TestServiceNoAnnotatedFields extends TestServiceNoFields {
        
        private TestConfiguration configuration = null;
        private ConfigurationManager configurationManager = null;
        private TestServiceNoFields service = null;
        private AgentServiceProvider serviceProvider = null;
        private AgentCredentialsManager credentialsManager = null;

        public TestConfiguration getConfiguration() {
            return configuration;
        }

        public ConfigurationManager getConfigurationManager() {
            return configurationManager;
        }

        public TestServiceNoFields getTestServiceNoFields() {
            return service;
        }

        public AgentServiceProvider getAgentServiceProvider() {
            return serviceProvider;
        }

        public AgentCredentialsManager getCredentialsManager() {
            return credentialsManager;
        }
    }
    
    /**
     * test service with no fields
     */
    class TestServiceNoFields implements AgentService {

        @Override
        public void init(AgentContext agentContext) throws AbstractAgentException {
        }

        @Override
        public void start() throws AbstractAgentException {
        }

        @Override
        public void stop() throws AbstractAgentException {
        }

        @Override
        public boolean isStarted() {
            return false;
        }
    }

    /**
     * test configuration class
     */
    class TestConfiguration implements Configuration {
        
    }
}
