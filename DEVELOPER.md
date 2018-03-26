# Developer Information

## Components

The agent is parted into different components:

### App
```
package com.telekom.cot.device.agent.app;
```
* This package contains the logic called when the app is started. It manages the *startup* phase and then starts the *cycle* phase by calling the other components described below.
  * The startup phase is responsible for connecting the device to CoT and updating the device data in the inventory.

  ![Startup phase](https://www.cumulocity.com/guides/rest/startupphase.png)
  * The cycle phase is responsible for sending measurements, events, alarms, executing operations.
  
  ![Cycle phase](https://www.cumulocity.com/guides/rest/cyclephase.png)
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
This service provides a generic interface for other services to communicate with the CoT. It contains the [CoT Java SDK](https://github.com/cloud-of-things/cot-java-rest-sdk) to communicate with the HTTP Rest API of the CoT.

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

A `SensorDeviceService` collects measurements at a fixed rate (e.g. `agent.raspbian.sensors.cputemperature.recordReadingsInterval`), checks for alarms and sends them to the SensorService.

Two examples of `SensorDeviceServices` are included in the project:
* `com.telekom.cot.device.agent.demo.sensor.DemoTemperatureSensor`
* `com.telekom.cot.device.agent.raspbian.sensor.CpuTemperatureSensor`

### SensorService
```
package com.telekom.cot.device.agent.sensor;
```
This service sends the measurements (that were pushed by the `SensorDeviceServices`) at a fixed rate to the CoT. It is also responsible to forward an incoming alarm from a SensorDeviceService immediately to the CoT, and to send events.

Dependencies:
* PlatformService

### OperationsHandlerService

`OperationsHandlerServices` are customer specific components (implementing the interface `OperationsHandlerService`) that support given operations (e.g. c8y_Restart, c8y_Configuration, c8y_SoftwareList).

An example of `OperationsHandlerServices` is included in the project:
* `com.telekom.cot.device.agent.raspbian.operation.RaspbianOperationsHandler`

### OperationService
```
package com.telekom.cot.device.agent.operation;
```
The `OperationService` is responsible for:
* Verifying that all registered `OperationsHandlerServices` handle different operations (there must be only one handler per operation)
* Starting all registered `OperationsHandlerServices`
* Updating the operations supported by the agent in the CoT
* Getting all pending operations from the CoT
* Letting the registered `OperationsHandlerServices` handle the operations
* Updating the operation statuses in the CoT

Dependencies:
* PlatformService

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
mvn -version
javac -version
```

To build the agent simply run:

```
mvn clean install
```

## Extending

Extending the agent in order to support customer specific devices requires the following steps:

* Create Java classes that implements the interfaces `SensorDeviceService` and/or `OperationsHandlerService`.
* Create a jar file with the class and an additional text file "META-INF/services/com.telekom.cot.device.agent.service.AgentService". The text file needs to contain the fully-qualified class name of the Java class.
* Copy the jar file to the "lib" folder of the agent.

The customer specific devices implementations will then be automatically *discovered* by the agent and will be used to collect/send measurements, events and alarms, or to handle specific operations depending on the implementation.

Two extension examples are included in the project:
* [demo](https://infinity-wbench.psst.t-online.corp/gitlab/Nova/cot-java-agent/tree/develop/demo)
* [raspbian](https://infinity-wbench.psst.t-online.corp/gitlab/Nova/cot-java-agent/tree/develop/raspbian)

### Sensors

Supporting new sensors requires creating a class implementing the interface `SensorDeviceService`.

`TemperatureSensor` is an abstract class implementing `SensorDeviceService` and can be extended to create a custom class. A thread is started in the `start()` method in order to measure sensor values at a fixed rate (defined in agent.yaml: recordReadingsInterval). This thead has the following tasks:
* Read a measurement
* Check for alarms
* Send the measurement to the `SensorService` so that it will be sent to the CoT in another thread

When a sensor value is measured, alarms can be checked and sent to the `SensorService` in case the measured value falls in the defined interval. See the method `checkAlarms()` in [TemperatureSensor](https://infinity-wbench.psst.t-online.corp/gitlab/Nova/cot-java-agent/blob/develop/sensor/src/main/java/com/telekom/cot/device/agent/sensor/deviceservices/TemperatureSensor.java). Alarms sent to `SensorService` are then immediately sent to the CoT.

Two sensor examples are included in the project:
* demo: [DemoTemperatureSensor.java](https://infinity-wbench.psst.t-online.corp/gitlab/Nova/cot-java-agent/blob/develop/demo/src/main/java/com/telekom/cot/device/agent/demo/sensor/DemoTemperatureSensor.java)
* raspbian: [CpuTemperatureSensor.java](https://infinity-wbench.psst.t-online.corp/gitlab/Nova/cot-java-agent/blob/develop/raspbian/src/main/java/com/telekom/cot/device/agent/raspbian/sensor/CpuTemperatureSensor.java)

### Operations

Supporting new operations requires creating a Java class implementing the interface `OperationsHandlerService`.

This interface contains following methods:
* public String[] getSupportedOperations()
* public OperationStatus execute(Operation operation)

`getSupportedOperations` should return the list of operations supported by the handler (e.g. "c8y_Restart").

`execute` is the method that executes a pending operation. It must return its status after the execution is finished (SUCCESSFUL, FAILED).

An operation handler is included in the project:
* raspbian: [com.telekom.cot.device.agent.raspian.operation](https://infinity-wbench.psst.t-online.corp/gitlab/Nova/cot-java-agent/tree/develop/raspbian/src/main/java/com/telekom/cot/device/agent/raspbian/operation)
