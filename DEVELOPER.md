# Developer Information

## Components

The agent is parted into different components:

### App
```
package com.telekom.cot.device.agent.app;
```
* This package contains the logic called when the app is started. It manages the *startup* phase and then starts the *cycle* phase by calling the other components described below.
  * The startup phase is responsible for connecting the device to CoT and updating the device data in the inventory.

  ![Startup phase](doc/startupphase.jpg)
  * The cycle phase is responsible for sending measurements, events, alarms, executing operations.
  
  ![Cycle phase](doc/cyclephase.jpg)
* This package also contains the logic called when the app is stopped (a shutdown hook permits to stop all services gracefully).

### Common

#### Exceptions
```
package com.telekom.cot.device.agent.common.exc;
```
This package contains all custom exceptions thrown by the agent.

#### Utilities
```
package com.telekom.cot.device.agent.common.util;
```
This component contains all helper classes.

### PlatformService
```
package com.telekom.cot.device.agent.platform;
```
This service provides a generic interface for other services to communicate with the CoT. 

### PlatformServiceRest
```
package com.telekom.cot.device.agent.platform.rest;
```
This package contains the configuration and implementation of the agent to use the REST protocol. It contains the [CoT Java SDK](https://github.com/cloud-of-things/cot-java-rest-sdk) to communicate with the HTTP Rest API of the CoT.

### PlatformServiceMqtt
```
package com.telekom.cot.device.agent.platform.mqtt;
```
This package contains the configuration and implementation of the agent to use the MQTT protocol. It contains the [MQTT SDK https://github.com/cloud-of-things/cot-mqtt-sdk) to communicate with MQTT Broker of the CoT. 

### Alarm
```
package com.telekom.cot.device.agent.alarm;
```
This package contains the interface and implementation to interact with alarms over the CoT.

### Event
```
package com.telekom.cot.device.agent.event;
```
This package contains the interface and implementation to interact with events over the CoT.

### CredentialsService
```
package com.telekom.cot.device.agent.credentials;
```
This service is responsible for:
* Checking if local device credentials are available (in a file called `device-credentials.yaml`).
* Requesting device credentials using the bootstrap credentials. If a device agent has its first contact with the CoT and no local device credentials are provided, this service requests device credentials. The device can then be accepted in the CoT and once the device credentials are retrieved, the agent is registered in the CoT. The device credentials are saved locally in a file called `device-credentials.yaml`.

Dependencies:
* PlatformService

### InventoryService
```
package com.telekom.cot.device.agent.inventory;
```
This service is responsible for:
* Checking if a device is already registered in the CoT.
* Creating the device in the inventory.
  * A managed object is created with name=`agent.services.inventoryService.deviceName`, type=`agent.services.inventoryService.deviceType` and fragments of type `c8y_IsDevice`, `com_cumulocity_model_Agent`, `c8y_Hardware`, `c8y_Firmware`, `c8y_Mobile`, `c8y_SoftwareList`, `c8y_Configuration`.
  * Hardware, firmware and mobile properties are read in the `agent.yaml` configuration file.
  * The software list is built dynamically by reading the names and versions of all internal packages.
* Registering the device (creating an external ID `agent.services.platformService.externalIdConfig` linked with the managed object).
* Updating the device in the inventory.

Dependencies:
* PlatformService

### Service
```
package com.telekom.cot.device.agent.service;
```
This package contains the logic to manage the services (e.g. load and initialize agent services from existing packages at class path, get services).

### SensorDeviceServices

`SensorDeviceServices` are customer specific components (implementing the interface `SensorDeviceService`) that handle all communication between the agent and sensor devices or subdevices.

A `SensorDeviceService` collects measurements at a fixed rate (e.g. `agent.raspbian.sensors.cputemperature.recordReadingsInterval`), checks for alarms and sends them to the PlatformService.

Two examples of `SensorDeviceServices` are included in the project:
* `com.telekom.cot.device.agent.demo.sensor.DemoTemperatureSensor`
* `com.telekom.cot.device.agent.raspbian.sensor.CpuTemperatureSensor`

### MeasurementService
```
package com.telekom.cot.device.agent.measurement;
```
This service sends the measurements (that were pushed by the `SensorDeviceServices`) at a fixed rate to the CoT. It is also responsible to forward an incoming alarm from a SensorDeviceService immediately to the CoT, and to send events.

Dependencies:
* PlatformService

### OperationService
```
package com.telekom.cot.device.agent.operation;
```
The `OperationService` is responsible for:
* Verifying that all registered `OperationHandlerServices` handle different operations (there must be only one handler per operation)
* Starting all registered `OperationHandlerServices`
* Updating the operations supported by the agent in the CoT
* Getting all pending operations from the CoT
* Letting the registered `OperationHandlerServices` handle the operations
* Updating the operation statuses in the CoT

Dependencies:
* PlatformService

### OperationHandlerService
```
package com.telekom.cot.device.agent.operation.handler;
```

`OperationHandlerServices` are customer specific services that handle and execute specific operations. Each operation handling service has to implement the interface `OperationHandlerService<T extends Operation>` for exactly one specific type of operation. The current implementation of the agent supports only exactly one `OperationHandlerService` per specific operation.

The project contains currently 4 `OperationHandlerService` implementations (at packages `com.telekom.cot.device.agent.operation.handler` and `com.telekom.cot.device.agent.raspbian.operation`):
* `ConfigurationUpdateOperationHandler` handles operations of type `ConfigurationUpdateOperation` and performs configuration updates
* `SoftwareUpdateOperationHandler` handles operations of type `SoftwareUpdateOperation` and performs agent software updates
* `TestOperationHandler` handles operations of type `TestOperation` which is used for automated tests
* `RestartOperationHandler` handles operations of type `RestartOperation` to restart a RaspberryPi device

### SystemService
```
package com.telekom.cot.device.agent.system;
```
The `SystemService` is the first service started in the bootstrap process. It initalizes the system by reading the configuration properties (stored in agent.yaml), the mobile properties (also stored in agent.yaml) and the software properties (by reading the name and version of the main package).

Two implementations of `SystemService` are included in the project:
* `com.telekom.cot.device.agent.demo.system.DemoSystemService`: Hardware and firmware properties are read from the agent.yaml configuration file.
* `com.telekom.cot.device.agent.raspbian.system.RaspbianSystemService`: Hardware and firmware properties are read from the raspbian system by executing system commands.

## Building

Before building please ensure you have at least JDK 1.8 and Maven 3. You can check this by running:

```
$ mvn -version
  maven version "3.x.x"

$ javac -version
  java version "1.8.x_xxx"
```

To build the agent simply run:

```
$ mvn clean install
```

Note: For further information according Maven see Apache Maven Project, e.g. 
* [Maven in 5 minutes](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)
* [Introduction to the POM](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html)
* [Introduction to the Build Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
* [Introduction to Repositories](https://maven.apache.org/guides/introduction/introduction-to-repositories.html)


## Extending

Extending the agent in order to support customer specific devices requires the following steps:

* Create Java classes that implements the interfaces `SensorDeviceService` and/or `OperationsHandlerService`.
* Create a jar file with the class and an additional text file "META-INF/services/com.telekom.cot.device.agent.service.AgentService". The text file needs to contain the fully-qualified class name of the Java class.
* Copy the jar file to the "lib" folder of the agent.

The customer specific devices implementations will then be automatically *discovered* by the agent and will be used to collect/send measurements, events and alarms, or to handle specific operations depending on the implementation.

Two extension examples are included in the project:
* [demo](demo)
* [raspbian](raspbian)

### Sensors

Supporting new sensors requires creating a class implementing the interface `SensorDeviceService`.

`TemperatureSensor` is an abstract class implementing `SensorDeviceService` and can be extended to create a custom class. A thread is started in the `start()` method in order to measure sensor values at a fixed rate (defined in agent.yaml: recordReadingsInterval). This thread has the following tasks:
* Read a measurement
* Check for alarms
* Send the measurement to the `MeasurementService` so that it will be sent to the CoT in another thread

When a sensor value is measured, alarms can be checked and sent to the `MeasurementService` in case the measured value falls in the defined interval. See the method `checkAlarms()` in [TemperatureSensor](sensor/src/main/java/com/telekom/cot/device/agent/sensor/deviceservices/TemperatureSensor.java). Alarms sent to `AlarmService` are then immediately sent to the CoT.

Two sensor examples are included in the project:
* demo: [DemoTemperatureSensor.java](demo/src/main/java/com/telekom/cot/device/agent/demo/sensor/DemoTemperatureSensor.java)
* raspbian: [CpuTemperatureSensor.java](raspbian/src/main/java/com/telekom/cot/device/agent/raspbian/sensor/CpuTemperatureSensor.java)

### Operations

Supporting new operations can be done in two steps:
1. <b>Implement a new specific operation type</b><br/>
by extending abstract class `com.telekom.cot.device.agent.platform.objects.operation.Operation`.<br/>
The new specific operation class must be annotated by the `com.telekom.cot.device.agent.platform.objects.operation.OperationAttributes` to assign the name of the operation (e.g. "c8y_Restart" for the `RestartOperation`). The JSON representation of the operation (sent from CoT) must contain this name as property.<br/><br/>
2. <b>Implement a new operation handler for the new operation type</b><br/>
by implementing the interface `OperationHandlerService<T extends Operation>`

The operation handler has two methods:<br/>
* <i>public Class<T> getSupportedOperationType()</i><br/>
returns the type of the new operation this handler can handle (e.g. `RestartOperation.class`)<br/><br/>
* <i>public OperationStatus execute(T operation)</i><br/>
handles operations of the new type and returns a status of the operation handling (e.g. `Operation.OperationStatus.SUCCESSFUL`)

The agent project contains currently 4 operation handler implementations:
* `ConfigurationUpdateOperationHandler` handles operations of type `ConfigurationUpdateOperation` and performs configuration updates
* `SoftwareUpdateOperationHandler` handles operations of type `SoftwareUpdateOperation` and performs agent software updates
* `TestOperationHandler` handles operations of type `TestOperation` which is used for automated tests
* `RestartOperationHandler` handles operations of type `RestartOperation` to restart a RaspberryPi device
