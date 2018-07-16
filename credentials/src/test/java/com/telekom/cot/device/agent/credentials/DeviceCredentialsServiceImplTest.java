package com.telekom.cot.device.agent.credentials;

public class DeviceCredentialsServiceImplTest {

//    private static final String EXTERNAL_ID_VALUE = "externalIdValue";
//    private static final String HARDWARE_SERIAL = "sn12345678";
//
//    @Rule public ExpectedException exceptions = ExpectedException.none();
//
//    @Mock private AgentServiceProvider mockServiceProvider;
//    @Mock private ConfigurationManager mockConfigurationManager;
//    @Mock private AgentCredentialsManager mockAgentCredentialsManager;
//    @Mock private PlatformService mockPlatformService;
//    @Mock private SystemService mockSystemService;
//    @Mock private Logger mockLogger;
//
//    private DeviceCredentialsServiceImpl registrationServiceImpl = new DeviceCredentialsServiceImpl();
//    private AgentCredentials agentCredentials = new AgentCredentials(true, "testTenant", "testUser", "testPassword");
//    private DeviceCredentialsServiceConfiguration configuration;
//    private HardwareProperties hardwareProperties;
//
//    @Before
//    public void setUp() throws Exception {
//        // initialize mocks
//        MockitoAnnotations.initMocks(this);
//
//        // initialize configuration
//        configuration = new DeviceCredentialsServiceConfiguration();
//        configuration.setInterval(1);
//        configuration.setDeviceIdTemplate(DeviceCredentialsServiceConfiguration.DeviceIdTemplates.EXTERNAL_ID_VALUE);
//        hardwareProperties = new HardwareProperties();
//        hardwareProperties.setSerialNumber(HARDWARE_SERIAL);
//
//        // inject mock "AgentServiceProvider" and configuration
//        InjectionUtil.inject(registrationServiceImpl, mockServiceProvider);
//        InjectionUtil.inject(registrationServiceImpl, mockConfigurationManager);
//        InjectionUtil.inject(registrationServiceImpl, mockAgentCredentialsManager);
//        InjectionUtil.inject(registrationServiceImpl, mockLogger);
//
//        // behavior of mocked AgentServiceProvider
//        when(mockServiceProvider.getService(PlatformService.class)).thenReturn(mockPlatformService);
//        when(mockServiceProvider.getService(SystemService.class)).thenReturn(mockSystemService);
//
//        // behavior of mocked ConfigurationManager
//        when(mockConfigurationManager.getConfiguration(DeviceCredentialsServiceConfiguration.class)).thenReturn(configuration);
//
//        // behavior of mocked SystemService
//        when(mockSystemService.getProperties(HardwareProperties.class)).thenReturn(hardwareProperties);
//
//        // behavior of mocked PlatformService
//        doNothing().when(mockPlatformService).start();
//        doNothing().when(mockPlatformService).stop();
//        when(mockPlatformService.getDeviceCredentials(EXTERNAL_ID_VALUE)).thenReturn(agentCredentials);
//        when(mockPlatformService.getDeviceCredentials(HARDWARE_SERIAL)).thenReturn(agentCredentials);
//        when(mockPlatformService.getExternalIdValue()).thenReturn(EXTERNAL_ID_VALUE);
//    }
//
//    @Test(expected = AgentServiceNotFoundException.class)
//    public void testStartNoPlatformService() throws AbstractAgentException {
//        // given
//        reset(mockServiceProvider);
//        when(mockServiceProvider.getService(PlatformService.class)).thenThrow(new AgentServiceNotFoundException(""));
//
//        // when
//        registrationServiceImpl.start();
//    }
//
//    @Test(expected = PropertyNotFoundException.class)
//    public void testStartNoConfigurationProperties() throws AbstractAgentException {
//        // given
//        when(mockConfigurationManager.getConfiguration(DeviceCredentialsServiceConfiguration.class)).thenThrow(new PropertyNotFoundException(""));
//
//        // when
//        registrationServiceImpl.start();
//    }
//
//    @Test
//    public void testStart() throws AbstractAgentException {
//        // when
//        registrationServiceImpl.start();
//
//        // then
//        assertTrue(registrationServiceImpl.isStarted());
//    }
//
//    /**
//     * test getDeviceId with deviceIdTemplate = null
//     */
//    @Test(expected=AbstractAgentException.class)
//    public void testGetDeviceIdNoTemplate() throws AbstractAgentException {
//        configuration.setDeviceIdTemplate(null);
//        registrationServiceImpl.start();
//        registrationServiceImpl.getDeviceId();
//    }
//
//    /**
//     * test getDeviceId with deviceIdTemplate = EXTERNAL_ID_VALUE
//     */
//    @Test
//    public void testGetDeviceIdWithExternalIdValueTemplate() throws AbstractAgentException {
//        //given
//
//        // when
//        registrationServiceImpl.start();
//
//        // then
//        assertEquals(EXTERNAL_ID_VALUE, registrationServiceImpl.getDeviceId());
//    }
//
//    /**
//     * test getDeviceId with deviceIdTemplate = HARDWARE_SERIAL
//     */
//    @Test
//    public void testGetDeviceIdWithHardwareSerialTemplate() throws AbstractAgentException {
//        //given
//        configuration.setDeviceIdTemplate(DeviceCredentialsServiceConfiguration.DeviceIdTemplates.HARDWARE_SERIAL);
//
//        // when
//        registrationServiceImpl.start();
//
//        // then
//        assertEquals(HARDWARE_SERIAL, registrationServiceImpl.getDeviceId());
//    }

//    @Test
//    public void testCredentialsAvailable() throws AbstractAgentException {
//        //given
//
//        // when
//        registrationServiceImpl.start();
//
//        // then
//        assertTrue(registrationServiceImpl.credentialsAvailable());
//        verify(mockLogger).info("found local device credentials");
//    }

//    @Test
//    public void testCredentialsNotAvailable() throws AbstractAgentException {
//        //given
//        AgentCredentialsNotFoundException e = new AgentCredentialsNotFoundException("Test");
//        when(mockAgentCredentialsManager.getCredentials()).thenThrow(e);
//
//        // when
//        registrationServiceImpl.start();
//
//        // then
//        assertFalse(registrationServiceImpl.credentialsAvailable());
//        verify(mockLogger).info("found no local device credentials", e);
//    }

//    @Test
//    public void testRequestCredentials() throws AbstractAgentException {
//        //when
//        registrationServiceImpl.start();
//
//        assertSame(agentCredentials, registrationServiceImpl.requestCredentials());
//    }
//
//    @Test(expected = CredentialsServiceException.class)
//    public void testRequestCredentialsExceptionHttpCode500() throws AbstractAgentException {
//        //prepare platformServiceMock
//        PlatformServiceException platformServiceException = new PlatformServiceException(500, "Test", null);
//        when(mockPlatformService.getDeviceCredentials(EXTERNAL_ID_VALUE)).thenThrow(platformServiceException);
//
//        //when
//        registrationServiceImpl.start();
//        assertSame(agentCredentials, registrationServiceImpl.requestCredentials());
//    }
//
//    @Test
//    public void testRequestCredentialsExceptionNotFound() throws AbstractAgentException {
//
//        Answer<AgentCredentials> requestCredentialsAnswer = new Answer<AgentCredentials>() {
//            private int requestCounter = 0;
//            
//            @Override
//            public AgentCredentials answer(InvocationOnMock invocation) throws Throwable {
//                requestCounter++;
//                if (requestCounter == 1) {
//                    throw new PlatformServiceException(404, "Test", null); 
//                }
//                return agentCredentials;
//            }
//            
//        };
//        
//        //prepare platformServiceMock
//        when(mockPlatformService.getDeviceCredentials(EXTERNAL_ID_VALUE)).then(requestCredentialsAnswer);
//
//        //when
//        registrationServiceImpl.start();
//        assertSame(agentCredentials, registrationServiceImpl.requestCredentials());
//        verify(mockLogger, times(2)).debug("try to get device credentials");
//    }
//
//    @Test
//    public void testRequestCredentialsInterrupted() throws Exception {
//        //prepare platformServiceMock
//        PlatformServiceException platformServiceException = new PlatformServiceException(404, "Test", null);
//        doThrow(platformServiceException).when(mockPlatformService).getDeviceCredentials(EXTERNAL_ID_VALUE);
//
//        //when
//        registrationServiceImpl.start();
//        
//        // call requestCredentials in a thread
//        Thread requestCredentialsThread = new Thread() {
//            @Override
//            public void run() {
//                try {
//                    registrationServiceImpl.requestCredentials();
//                } catch (AbstractAgentException e) {
//                }
//            }            
//        };
//        requestCredentialsThread.start();
//        Thread.sleep(100);
//        requestCredentialsThread.interrupt();
//        Thread.sleep(200);
//        
//        verify(mockLogger).error(eq("interrupted exception"), any(InterruptedException.class));
//    }

}
