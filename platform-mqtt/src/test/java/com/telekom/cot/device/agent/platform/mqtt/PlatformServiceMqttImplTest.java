package com.telekom.cot.device.agent.platform.mqtt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.configuration.AgentCredentials;
import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.platform.mqtt.PlatformServiceMqttConfiguration.MqttConfiguration;
import com.telekom.cot.device.agent.platform.mqtt.event.AlarmAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.ManagedObjectAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.StartupAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.TemperatureAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEventListener;
import com.telekom.cot.device.agent.platform.objects.AgentConfiguration;
import com.telekom.cot.device.agent.platform.objects.AgentFirmware;
import com.telekom.cot.device.agent.platform.objects.AgentHardware;
import com.telekom.cot.device.agent.platform.objects.AgentManagedObject;
import com.telekom.cot.device.agent.platform.objects.AgentMobile;
import com.telekom.cot.device.agent.platform.objects.AgentSoftwareList;
import com.telekom.cot.device.agent.platform.objects.ManagedObject;
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.platform.objects.operation.Operation;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;
import com.telekom.cot.device.agent.service.event.AgentContext;
import com.telekom.cot.device.agent.service.event.AgentEvent;
import com.telekom.cot.device.agent.service.event.AgentEventPublisher;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.MobileProperties;

import de.tarent.telekom.cot.mqtt.util.JsonHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MqttPlatformBuilder.class)
public class PlatformServiceMqttImplTest {

	private static final String ICC_ID = "12345";
	private static final String X_ID = "unitTestTemplates";

	@Mock
	private MqttPlatformBuilder mockMqttPlatformBuilder;
	@Mock
	private MqttPlatform mockMqttPlatform;
	@Mock
	private SystemService mockSystemService;
	@Mock
	private MobileProperties mockMobileProperties;
	@Mock
	private AgentCredentialsManager mockAgentCredentialsManager;
	@Mock
	private AgentContext mockAgentContext;
	@Mock
	private ConcurrentLinkedQueue<Operation> mockConcurrentLinkedQueue;
	@Mock
	private PublishCallback mockPublishCallback;
	@Mock
	private AgentEventPublisher mockAgentEventPublisher;
	@Mock
	private ManagedObjectAgentEventListener mockManagedObjectAgentEventListener;
	@Mock
	private UpdateFragmentAgentEventListener mockUpdateInventoryAgentEventListener;
	@Mock
	private TemperatureAgentEventListener mockTemperatureAgentEventListener;
	@Mock
	private StartupAgentEventListener mockStartupAgentEventListener;
	@Mock
	private AlarmAgentEventListener mockAlarmAgentEventListener;

	@Captor
	private ArgumentCaptor<Operation> captor;

	private PlatformServiceMqttImpl platformServiceMqtt = new PlatformServiceMqttImpl();
	private PlatformServiceMqttConfiguration platformServiceMqttConfiguration = new PlatformServiceMqttConfiguration();
	private AgentCredentials agentCredentials = new AgentCredentials();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(MqttPlatformBuilder.class);
		PowerMockito.when(MqttPlatformBuilder.create(any(), any())).thenReturn(mockMqttPlatformBuilder);

		when(mockMqttPlatformBuilder.setVerticalWorkerPoolSize(PlatformServiceMqttImpl.VERTICAL_WORKER_POOL_SIZE))
				.thenReturn(mockMqttPlatformBuilder);
		when(mockMqttPlatformBuilder.setVertxEventLoopPoolSize(PlatformServiceMqttImpl.VERTX_EVENT_LOOP_POOL_SIZE))
				.thenReturn(mockMqttPlatformBuilder);
		when(mockMqttPlatformBuilder
				.setVertxInternalBlockingPoolSize(PlatformServiceMqttImpl.VERTX_INTERNAL_BLOCKING_POOL_SIZE))
						.thenReturn(mockMqttPlatformBuilder);
		when(mockMqttPlatformBuilder.setVertxWorkerPoolSize(PlatformServiceMqttImpl.VERTX_WORKER_POOL_SIZE))
				.thenReturn(mockMqttPlatformBuilder);
		doAnswer(new Answer<MqttPlatform>() {

			@Override
			public MqttPlatform answer(InvocationOnMock invocation) throws Throwable {
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {

					@SuppressWarnings("unchecked")
					@Override
					public void run() {
						((Consumer<Boolean>) invocation.getArguments()[0]).accept(true);
					}
				}, 1000);
				return mockMqttPlatform;
			}
		}).when(mockMqttPlatformBuilder).build(any());

		// mock SystemService and iccId
		when(mockMobileProperties.getIccid()).thenReturn(ICC_ID);
		when(mockSystemService.getProperties(MobileProperties.class)).thenReturn(mockMobileProperties);
		when(mockAgentContext.getAgentEventPublisher(any())).thenReturn(mockAgentEventPublisher);

		// platformServiceMqttConfiguration
		MqttConfiguration mqttConfiguration = new MqttConfiguration();
		mqttConfiguration.setDelaySendMeasurement(100);
		platformServiceMqttConfiguration.setMqttConfiguration(mqttConfiguration);
		platformServiceMqttConfiguration.getMqttConfiguration().setxId(X_ID);
		platformServiceMqttConfiguration.getMqttConfiguration().setPort("1234");
		platformServiceMqttConfiguration.getMqttConfiguration().setTimeout(5);
		platformServiceMqttConfiguration.getMqttConfiguration().setDelaySendMeasurement(100);
		platformServiceMqttConfiguration.setHostName("localhost");

		// inject other mocks
		InjectionUtil.inject(platformServiceMqtt, mockSystemService);
		InjectionUtil.inject(platformServiceMqtt, platformServiceMqttConfiguration);
		InjectionUtil.inject(platformServiceMqtt, mockAgentCredentialsManager);
		InjectionUtil.inject(platformServiceMqtt, mockMqttPlatform);
		InjectionUtil.inject(platformServiceMqtt, mockAgentContext);
		InjectionUtil.inject(platformServiceMqtt, mockManagedObjectAgentEventListener);
		InjectionUtil.inject(platformServiceMqtt, mockUpdateInventoryAgentEventListener);
		InjectionUtil.inject(platformServiceMqtt, mockConcurrentLinkedQueue);
		InjectionUtil.inject(platformServiceMqtt, mockTemperatureAgentEventListener);
		InjectionUtil.inject(platformServiceMqtt, mockStartupAgentEventListener);
		InjectionUtil.inject(platformServiceMqtt, mockAlarmAgentEventListener);
	}

	/**
	 * test method start
	 **/
	@Test
	public void testStartAgentMqttBootstrapping() throws AbstractAgentException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("management");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		// behavior
		platformServiceMqtt.start();
		// assert
		Assert.assertThat(platformServiceMqtt.isStarted(), Matchers.equalTo(true));
	}

	/**
	 * 
	 * test method start
	 * 
	 * @throws InterruptedException
	 **/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testStartAgentMqttBootstrapped() throws AbstractAgentException, InterruptedException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(false);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		when(mockManagedObjectAgentEventListener.waitOnAgentEventAndCreate()).thenReturn(new ManagedObject("1"));
		// setup callback
		doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[0]).accept(true);
				return null;
			}
		}).when(mockMqttPlatform).subscribeToTopic(any(), any());
		doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				Consumer<Boolean> callback = (Consumer<Boolean>) invocationOnMock.getArguments()[1];
				callback.accept(true);
				return null;
			}
		}).when(mockMqttPlatform).publishMessage(any(), any());
		AgentEvent testEvent = new AgentEvent() {
			@Override
			public Object getSource() {
				return null;
			}
		};
		// behavior
		platformServiceMqtt.start();
		mockAgentEventPublisher.publishEvent(testEvent);

		// assert
		Assert.assertThat(platformServiceMqtt.isStarted(), Matchers.equalTo(true));
		verify(mockAgentEventPublisher).publishEvent(any());
	}

	/**
	 * 
	 * test method start
	 * 
	 * @throws InterruptedException
	 **/
	@Test
	public void testStartAgentMqttBootstrappedSubscribeToTopicTimeout()
			throws AbstractAgentException, InterruptedException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(false);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		// setup callback
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						((Consumer<Object>) invocationOnMock.getArguments()[0]).accept(true);
					}
				}, 6000);
				return null;
			}
		}).when(mockMqttPlatform).subscribeToTopic(any(), any());
		// behavior
		try {
			platformServiceMqtt.start();
			fail();
		} catch (AbstractAgentException agentException) {
			// ignore
		}
	}

	/**
	 * test method start
	 **/
	@Test(expected = PlatformServiceException.class)
	public void testStartAgentMqttBootstrappedAndErrorWhileSubscribing() throws AbstractAgentException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(false);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		// setup callback
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[0]).accept(false);
				return null;
			}
		}).when(mockMqttPlatform).subscribeToTopic(any(), any());
		// behavior
		platformServiceMqtt.start();
		// assert
		Assert.assertThat(platformServiceMqtt.isStarted(), Matchers.equalTo(true));
	}

	/**
	 * test method start
	 * 
	 * @throws InterruptedException
	 **/
	@Test(expected = PlatformServiceException.class)
	public void testStartAgentMqttBootstrappedAndErrorWhileFetchingManagedObjectId()
			throws AbstractAgentException, InterruptedException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(false);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		when(mockManagedObjectAgentEventListener.waitOnAgentEventAndCreate()).thenThrow(ExecutionException.class);
		// setup callback
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[0]).accept(true);
				return null;
			}
		}).when(mockMqttPlatform).subscribeToTopic(any(), any());
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[1]).accept(false);
				return null;
			}
		}).when(mockMqttPlatform).publishMessage(any(), any());
		// behavior
		platformServiceMqtt.start();
		// assert
		Assert.assertThat(platformServiceMqtt.isStarted(), Matchers.equalTo(true));
	}

	/**
	 * test method getDeviceCredentials
	 * 
	 * @throws InterruptedException
	 **/
	@Test
	public void testGetDeviceCredentials() throws AbstractAgentException, InterruptedException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("management");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		Properties props = new Properties();
		when(mockMqttPlatform.getProperties()).thenReturn(props);
		when(mockManagedObjectAgentEventListener.waitOnAgentEventAndCreate()).thenReturn(new ManagedObject("1"));
		// setup callback
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[0]).accept(true);
				return null;
			}
		}).when(mockMqttPlatform).subscribeToTopic(any(), any());
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[1]).accept(true);
				return null;
			}
		}).when(mockMqttPlatform).publishMessage(any(), any());
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<String>) invocationOnMock.getArguments()[0]).accept("password");
				return null;
			}
		}).when(mockMqttPlatform).registerDevice(any());
		// behavior
		platformServiceMqtt.start();
		// assert
		Assert.assertThat(platformServiceMqtt.getDeviceCredentials(ICC_ID, 1).getPassword(),
				Matchers.equalTo("password"));
		Assert.assertThat(props.getProperty(JsonHelper.PASSWORD_KEY), Matchers.equalTo("password"));
	}

	/**
	 * test method createMeasurement
	 **/
	@Test
	public void testCreateMeasurementSuccessfully() throws AbstractAgentException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		// setup callback
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[1]).accept(true);
				return null;
			}
		}).when(mockMqttPlatform).publishMessage(any(), any());
		// behavior
		platformServiceMqtt.start();
		platformServiceMqtt.createMeasurement(new Date(), "test", (float) 1.0, "°C");
		// assert
		verify(mockMqttPlatform).publishMessage(any(), any());
	}

	/**
	 * test method createMeasurement
	 **/
	@Test(expected = PlatformServiceException.class)
	public void testCreateMeasurementFailure() throws AbstractAgentException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		// setup callback
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[1]).accept(false);
				return null;
			}
		}).when(mockMqttPlatform).publishMessage(any(), any());
		// behavior
		platformServiceMqtt.start();
		platformServiceMqtt.createMeasurement(null, null, (float) 1.0, null);
		// assert
		verify(mockMqttPlatform, times(1)).publishMessage(any(), any());
	}

	/**
	 * test method createMeasurements
	 **/
	@Test
	public void testCreateMeasurementsSuccessfully() throws AbstractAgentException {
		// setup measurements
		List<SensorMeasurement> measurements = new ArrayList<>();
		SensorMeasurement sensorMeasurement = new SensorMeasurement("test", (float) 1.0, "°C");
		measurements.add(sensorMeasurement);
		sensorMeasurement.setType("test2");
		measurements.add(sensorMeasurement);
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		// setup callback
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[1]).accept(true);
				return null;
			}
		}).when(mockMqttPlatform).publishMessage(any(), any());
		// behavior
		platformServiceMqtt.start();
		platformServiceMqtt.createMeasurements(measurements);
		// assert
		verify(mockMqttPlatform, times(2)).publishMessage(any(), any());
	}

	/**
	 * test method createMeasurements
	 **/
	@Test
	public void testCreateMeasurementsEmpty() throws AbstractAgentException {
		// setup measurements
		List<SensorMeasurement> measurements = new ArrayList<>();
		platformServiceMqtt.createMeasurements(measurements);
		// assert
		verify(mockMqttPlatform, times(0)).publishMessage(any(), any());
	}

	/**
	 * test method createEvent
	 * 
	 * @throws InterruptedException
	 **/
	@Test
	public void testCreateEvent() throws AbstractAgentException, InterruptedException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		// setup callback
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[1]).accept(true);
				return null;
			}
		}).when(mockMqttPlatform).publishMessage(any(), any());
		// behavior
		platformServiceMqtt.start();
		platformServiceMqtt.createEvent(new Date(), "testEvent", "testEvent", "testEvent");
		// assert
		TimeUnit.MICROSECONDS.sleep(5000);
		verify(mockMqttPlatform, times(1)).publishMessage(any(), any());
	}

	/**
	 * test method createEvent
	 * @throws InterruptedException 
	 **/
	@Test(expected = PlatformServiceException.class)
	public void testCreateEventFailure() throws AbstractAgentException, InterruptedException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		// setup callback
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[1]).accept(false);
				return null;
			}
		}).when(mockMqttPlatform).publishMessage(any(), any());
		// behavior
		platformServiceMqtt.start();
		platformServiceMqtt.createEvent(null, null, null, null);
		// assert
		verify(mockMqttPlatform, times(1)).publishMessage(any(), any());
	}

	/**
	 * test method createAlarm
	 * 
	 * @throws InterruptedException
	 **/
	@Test
	public void testCreateAlarm() throws AbstractAgentException, InterruptedException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		// setup callback
		doAnswer(new Answer<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[1]).accept(true);
				return null;
			}
		}).when(mockMqttPlatform).publishMessage(any(), any());
		Date time = new Date();
		String type = "TemperatureAlarmWarning";
		AlarmSeverity severity = AlarmSeverity.WARNING;
		String text = "Warning alarm - Temperature reached a value of 20";
		String status = "ACTIVE";
		String managedObjectId = null;
		String template = SmartRestUtil.getPayloadCreateAlarm(X_ID, time, type, severity, text, status,
				managedObjectId);
		// behavior
		platformServiceMqtt.start();
		platformServiceMqtt.createAlarm(time, type, severity, text, status);
		// assert
		TimeUnit.MICROSECONDS.sleep(5000);
		verify(mockMqttPlatform).publishMessage(eq(template), any());
	}

	/**
	 * test method stop
	 **/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testStopAgentMqtt() throws AbstractAgentException {

		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);

		// setup callback
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[1]).accept(true);
				return null;
			}
		}).when(mockMqttPlatform).publishMessage(any(), any());
		platformServiceMqtt.start();

		AsyncResult asyncResult = Mockito.mock(AsyncResult.class);
		when(asyncResult.succeeded()).thenReturn(true);
		doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				new Timer().schedule(new TimerTask() {

					@Override
					public void run() {
						((Consumer<Object>) invocationOnMock.getArguments()[0]).accept(true);
					}
				}, 500);
				return true;
			}
		}).when(mockMqttPlatform).unsubscribeFromTopic(any());
		doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Handler<AsyncResult<Void>>) invocationOnMock.getArguments()[0]).handle(asyncResult);
				return true;
			}
		}).when(mockMqttPlatform).close(any());

		platformServiceMqtt.stop();
		Assert.assertThat(platformServiceMqtt.isStarted(), Matchers.equalTo(false));
	}

	/**
	 * test method getAgentManagedObject
	 */
	@Test
	public void testGetAgentManagedObject() throws AbstractAgentException {
		AgentManagedObject agentManagedObject = new AgentManagedObject();

		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		when(mockMobileProperties.getIccid()).thenReturn(ICC_ID);

		// setup callback
		doAnswer(new Answer<Void>() {
			@SuppressWarnings("unchecked")
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				((Consumer<Object>) invocationOnMock.getArguments()[1]).accept(true);
				return null;
			}
		}).when(mockMqttPlatform).publishMessage(any(), any());
		agentManagedObject.setName(ICC_ID);
		platformServiceMqtt.start();

		Assert.assertThat(platformServiceMqtt.getAgentManagedObject().getName(),
				Matchers.equalTo(agentManagedObject.getName()));
	}

	/**
	 * test method updateSupportedOperations
	 */
	@Test
	public void testUpdateSupportedOperations() throws AbstractAgentException, InterruptedException {
		List<String> supportedOperations = Arrays.asList("c8y_Test");

		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);

		// configure listener
		when(mockUpdateInventoryAgentEventListener.waitOnAgentEventAndCreate()).thenReturn("1");
		// setup callback
		AgentManagedObject agentManagedObject = new AgentManagedObject();
		// get the expected template
		String templateSupportedOperations = SmartRestUtil.getPayloadUpdateSupportedOperations(X_ID,
				agentManagedObject.getId(), supportedOperations);

		// behavior
		platformServiceMqtt.start();
		platformServiceMqtt.updateAgentManagedObject(agentManagedObject);
		platformServiceMqtt.updateSupportedOperations(supportedOperations);

		// assert
		Assert.assertThat(platformServiceMqtt.isStarted(), Matchers.equalTo(true));
		verify(mockMqttPlatform, times(1)).publishMessage(eq(templateSupportedOperations), any());
	}

	/**
	 * test updateAgentManagedObject (hardware fragment)
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testUpdateAgentManagedObjectHardware() throws AbstractAgentException, InterruptedException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);

		// configure listener
		when(mockUpdateInventoryAgentEventListener.waitOnAgentEventAndCreate()).thenReturn("1");

		// configure managed object with the fragment c8y_Hardware to update
		AgentManagedObject agentManagedObject = new AgentManagedObject();
		AgentHardware agentHardware = new AgentHardware("testModel", "testRevision", "testSerial");
		agentManagedObject.addFragment(agentHardware);

		// get the expected template
		String templateHardware = SmartRestUtil.getPayloadUpdateHardware(X_ID, agentManagedObject.getId(),
				agentHardware, false);

		// behavior
		platformServiceMqtt.start();
		platformServiceMqtt.updateAgentManagedObject(agentManagedObject);

		// assert
		Assert.assertThat(platformServiceMqtt.isStarted(), Matchers.equalTo(true));
		verify(mockMqttPlatform, times(1)).publishMessage(eq(templateHardware), any());
	}

	/**
	 * test updateAgentManagedObject (firmware fragment)
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testUpdateAgentManagedObjectFirmware() throws AbstractAgentException, InterruptedException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);

		// configure listener
		when(mockUpdateInventoryAgentEventListener.waitOnAgentEventAndCreate()).thenReturn("1");

		// configure managed object with the fragment c8y_Firmware to update
		AgentManagedObject agentManagedObject = new AgentManagedObject();
		AgentFirmware agentFirmware = new AgentFirmware("testFirmware", "testVersion", "testURL");
		agentManagedObject.addFragment(agentFirmware);

		// get the expected template
		String templateFirmware = SmartRestUtil.getPayloadUpdateFirmware(X_ID, agentManagedObject.getId(),
				agentFirmware, false);

		// behavior
		platformServiceMqtt.start();
		platformServiceMqtt.updateAgentManagedObject(agentManagedObject);

		// assert
		Assert.assertThat(platformServiceMqtt.isStarted(), Matchers.equalTo(true));
		verify(mockMqttPlatform, times(1)).publishMessage(eq(templateFirmware), any());
	}

	/**
	 * test updateAgentManagedObject (mobile fragment)
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testUpdateAgentManagedObjectMobile() throws AbstractAgentException, InterruptedException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);

		// configure listener
		when(mockUpdateInventoryAgentEventListener.waitOnAgentEventAndCreate()).thenReturn("1");

		// configure managed object with the fragment c8y_Mobile to update
		AgentManagedObject agentManagedObject = new AgentManagedObject();
		AgentMobile agentMobile = new AgentMobile("testImei", "testCellId", "testIccId");
		agentManagedObject.addFragment(agentMobile);

		// get the expected template
		String templateMobile = SmartRestUtil.getPayloadUpdateMobile(X_ID, agentManagedObject.getId(), agentMobile,
				false);

		// behavior
		platformServiceMqtt.start();
		platformServiceMqtt.updateAgentManagedObject(agentManagedObject);

		// assert
		Assert.assertThat(platformServiceMqtt.isStarted(), Matchers.equalTo(true));
		verify(mockMqttPlatform, times(1)).publishMessage(eq(templateMobile), any());
	}

	/**
	 * test updateAgentManagedObject (software list fragment)
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testUpdateAgentManagedObjectSoftwareList() throws AbstractAgentException, InterruptedException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);

		// configure listener
		when(mockUpdateInventoryAgentEventListener.waitOnAgentEventAndCreate()).thenReturn("1");

		// configure managed object with the fragment c8y_SoftwareList to update
		AgentManagedObject agentManagedObject = new AgentManagedObject();
		AgentSoftwareList agentSoftwareList = new AgentSoftwareList();
		AgentSoftwareList.Software software = new AgentSoftwareList.Software("testNameSoftware", "0815", "testURL");
		agentSoftwareList.addSoftware(software);
		agentManagedObject.addFragment(agentSoftwareList);

		// get the expected template
		String templateSoftwareList = SmartRestUtil.getPayloadUpdateSoftwareList(X_ID, agentManagedObject.getId(),
				software, false);

		// behavior
		platformServiceMqtt.start();
		platformServiceMqtt.updateAgentManagedObject(agentManagedObject);

		// assert
		Assert.assertThat(platformServiceMqtt.isStarted(), Matchers.equalTo(true));
		verify(mockMqttPlatform, times(1)).publishMessage(eq(templateSoftwareList), any());
	}

	/**
	 * test updateAgentManagedObject (configuration fragment)
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testUpdateAgentManagedObjectConfiguration() throws AbstractAgentException, InterruptedException {
		// configure agentCredentials
		agentCredentials.setBootstrappingMode(true);
		agentCredentials.setUsername("test");
		agentCredentials.setPassword("test");
		agentCredentials.setTenant("MQTT");
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);

		// configure listener
		when(mockUpdateInventoryAgentEventListener.waitOnAgentEventAndCreate()).thenReturn("1");

		// configure managed object with the fragment c8y_Configuration to update
		AgentManagedObject agentManagedObject = new AgentManagedObject();
		AgentConfiguration agentConfiguration = new AgentConfiguration("config");
		agentManagedObject.addFragment(agentConfiguration);

		// get the expected template
		String templateConfiguration = SmartRestUtil.getPayloadUpdateConfiguration(X_ID, agentManagedObject.getId(),
				agentConfiguration, false);

		// behavior
		platformServiceMqtt.start();
		platformServiceMqtt.updateAgentManagedObject(agentManagedObject);

		// assert
		Assert.assertThat(platformServiceMqtt.isStarted(), Matchers.equalTo(true));
		verify(mockMqttPlatform, times(1)).publishMessage(eq(templateConfiguration), any());
	}

	/**
	 * Get next pending operation from list - success
	 */
	@Test
	public void testGetNextPendingOperation_SUCCESS() throws AbstractAgentException {
		when(mockConcurrentLinkedQueue.poll()).thenReturn(new Operation("1") {});

		InjectionUtil.inject(platformServiceMqtt, mockConcurrentLinkedQueue);

		assertNotNull(platformServiceMqtt.getNextPendingOperation());

		verify(mockConcurrentLinkedQueue, times(1)).poll();

	}

	/**
	 * Get next pending operation from list - empty list
	 */
	@Test
	public void testGetNextPendingOperation_EMPTY() throws AbstractAgentException {
		when(mockConcurrentLinkedQueue.poll()).thenReturn(null);

		InjectionUtil.inject(platformServiceMqtt, mockConcurrentLinkedQueue);

		assertNull(platformServiceMqtt.getNextPendingOperation());

		verify(mockConcurrentLinkedQueue, times(1)).poll();
	}

	/**
	 *
	 * @throws AbstractAgentException
	 */
	@Test
	public void testUpdateOperationStatus() throws AbstractAgentException, InterruptedException {
		// mock the listener
		UpdateFragmentAgentEventListener mockListener = Mockito.mock(UpdateFragmentAgentEventListener.class);
		// inject listener and set timeout
		InjectionUtil.inject(platformServiceMqtt, mockListener);
		InjectionUtil.inject(platformServiceMqtt, "timeout", 1);
		// set behavior
		when(mockListener.waitOnAgentEventAndCreate()).thenReturn("1");
		doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				String message = invocation.getArgument(0);
				// verify message
				Assert.assertThat(message.indexOf("300,1,EXECUTING,EXECUTING") > 0, Matchers.equalTo(true));
				return null;
			}
		}).when(mockMqttPlatform).publishMessage(any(), any());
		// execute the test
		platformServiceMqtt.updateOperationStatus("1", OperationStatus.EXECUTING);
		// verify publishMessage
		verify(mockMqttPlatform).publishMessage(any(), any());
	}
}
