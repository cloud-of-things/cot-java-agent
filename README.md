# Cloud of Things Java Reference Agent

The CoT Java Reference Device Agent described in this document can be used as basic implementation for customer CoT Java Device Agents, e.g. for devices like Raspberry Pi or customer specific devices.

The [Cloud of Things](https://m2m.telekom.com/our-offering/cloud-of-things/) (German Cloud der Dinge) is a platform for the Internet of Things by T-Systems International GmbH. Inside this repository you will find a Java based Reference Agent.

This Java agent provides a reference implementation for device management features of the Cloud of Things as well as a library for the Raspberry Pi.

The Java agent contains following functionalities:
* Register device
* Update inventory
* Send measurements
* Send events
* Send alarms
* Execute operations
* Update configuration
* Update software

_Current version is: 0.11.0_

## Prerequisites

An installation of Java SE 8 is required. To verify the availability of Java on your system, type:

```
$ java -version
java version "1.8.0_101"
```

## Installing

A binary package is available for the Raspberry Pi.

```
$ wget http://<path_to_package>/device-agent-raspbian_0.11.0~SNAPSHOT_all.deb
$ sudo dpkg -i device-agent-raspbian_0.11.0~SNAPSHOT_all.deb
```

## Configuration

The agent configuration is defined in the [agent.yaml](app/agent.yaml) file.

The agent.yaml file contains sections for the main components:

### agent.common
```
shutdownTimeout: 2000
```
* `<shutdownTimeout>` is the common shutdown timeout in milliseconds. When the agent is stopped, all services are shut down. In this case, the agent waits until all tasks from the service to stop have completed execution, or the timeout occurs, whichever happens first.

### agent.properties
```
system:
  hardware:
    model:  "RaspPi BCM2708"
    revision: 000e
    serialNumber: 0000000017b769d5
  firmware:
    name: "raspberrypi-bootloader"
    version: "1.20130207-1"
    url: "http://loadnewversion.de/"
  mobile:
    imei: "861145013087177"
    cellId: "4904-A496"
    iccid: "89490200000876620613"
```
* `system.hardware` contains basic hardware information for a device, such as model and serial number. This information is used to update the fragment `c8y_Hardware` in the inventory when the agent is started.
* `system.firmware` contains information on a device's firmware. This information is used to update the fragment `c8y_Firmware` in the inventory when the agent is started.
* `system.mobile` holds basic connectivity-related information, such as the equipment identifier of the modem (IMEI) in the device. This information is used to update the fragment `c8y_Mobile` in the inventory when the agent is started.

### agent.services.inventoryService
```
deviceType: c8y_Linux
deviceName: testNovaAgent
isDevice: true
isAgent: true
```
These inventory properties are used in order to create the device in the inventory.
* A managed object representing the device is created (with the properties `<deviceName>` and `<deviceType>`).
* `<isDevice>`: if true, the fragment `c8y_IsDevice` is added in the inventory when the agent is started, the device is visible in the CoT and the agent is able to send measurements.
* `<isAgent>`: if true, the fragment `com_cumulocity_model_Agent` is added in the inventory when the agent is started, and the agent is able to execute operations.

### agent.services.platformService
```
proxyHost: 10.100.100.10
proxyPort: 8080
hostName: ram.m2m.telekom.com
externalIdConfig:
  valueTemplate: TYPE_HARDWARE_SERIAL
  type: novaAgent
  value: null
```
* `<proxyHost>` is the IP address or host name of the proxy server.
* `<proxyPort>` is the port number that is used by the proxy server for client connections.
* `<hostName>` is the https address of your CoT instance. "https" and the tenant will be automatically added by the agent to the hostname (`url = "https://" + tenant + "." + hostname`).
* `<valueTemplate>` is the template to generate the external id value. Possible values: NO_TEMPLATE, HARDWARE_SERIAL, TYPE_HARDWARE_SERIAL
  * `NO_TEMPLATE`: no template, 'value' is used.
  * `HARDWARE_SERIAL`: agent.properties.system.hardware.serialNumber is used.
  * `TYPE_HARDWARE_SERIAL`: a combination of external id type (agent.services.platformService.externalIdConfig.type) and hardware serial number (agent.properties.system.hardware.serialNumber) is used (e.g. novaAgent-0000000017b769d5)

### agent.services.deviceCredentialsService
```
deviceIdTemplate: EXTERNAL_ID_VALUE
interval: 10
bootstrapCredentials:
  username: <username>
  password: <password>
  tenant: management
```
* The deviceId is used to request device credentials when the device is being registered. 
  * When you take a new device into use, you enter this unique ID into "Device registration" in the CoT and start the agent.
  * The agent will connect to the CoT and send its unique ID repeatedly until the connection is accepted in the CoT.
  * You can accept the connection from the device in "Device registration", in which case the CoT sends the generated credentials to the agent.
* `<deviceIdTemplate>` is the template to generate the device id (used for device registration at the CoT). Possible values: EXTERNAL_ID_VALUE, HARDWARE_SERIAL
  * `EXTERNAL_ID_VALUE`: the external id value is used.
  * `HARDWARE_SERIAL`: agent.properties.system.hardware.serialNumber is used. 
* `<interval>` is  is the delay (in seconds) between each attempt to get the device credentials from the CoT.
* `bootstrapCredentials`: These bootstrap user properties are used when the agent is started and there is no device-credentials.yaml file. In this case the agent uses these properties to *register* the device in the CoT.

### agent.services.sensorService
```
sendInterval: 5
```
* `<sendInterval>` is the fixed rate (in seconds) at which measurements are sent to the CoT.

### agent.services.operationService
```
interval: 10
resultSize: 1
shutdownTimeout: 5000
handlersShutdownTimeout: 500 
```
* `<interval>` is the delay (in seconds) between each attempt to handle pending operations. Once the agent has handled all pending operations, it pauses for a given amount of time (`<interval>`) before checking if there are new pending operations to handle.
* `<resultSize>` is the number of requested operations.
* `<shutdownTimeout>` and `<handlersShutdownTimeout>` are shutdown timeout in milliseconds for stopping operation service and its operation handlers.

### agent.operations
```
softwareUpdate:
  executeScript: ""
  targetPath: ""
  algorithm: "MD5"
```
* Following actions must be performed in order to update the software through the CoT:
  * Create a zip file containing 2 files:
    * Debian package for the new version to install
    * Hash file of the debian package (MD5, SHA-1, SHA-256 are the supported hash algorithms)
  * Upload the zip file in the Software repository section of the CoT
  * Select the device, go to the 'Software' section, select the software to install, click 'Install', make sure only *one software* is displayed in the list and then click 'Submit operation'
* When a new software is installed through the CoT, a new operation of type `c8y_SoftwareList` is created. The agent handles this type of operation by performing following tasks:
  * Make sure that there is only one entry in the `c8y_SoftwareList` operation (before clicking on *Submit operation* when installing a new version, the user should make sure that only one software version is displayed in the list)
  * Download the software binary (the url of the new software to install is specified in the operation JSON payload)
  * Unzip the downloaded software in `<targetPath>`
  * Verify that the downloaded software is valid: for this a hash of the debian package is calculated and compared with the hash file provided in the zip file
  * Copy the current `agent.yaml` in `<targetPath>`
  * Execute the update script (path is specified in `executeScript`). This script should contain commands to perform the software update (i.e. stop the agent, install the new debian package, start the agent)
  * When the agent is restarted, the operation status is automatically set to `SUCCESSFUL`
* `<executeScript>` is the path to the script that performs the software update
* `<targetPath>` is the path where the software to install is unzipped
* `<algorithm>` is the hash algorithm to use to compute the hash of the downloaded debian package. Possible values: MD5, SHA-1, SHA-256

### agent.raspbian and agent.demo

* The project `device-agent-raspbian` provides raspbian device specific implementations for features like reading CPU temperature, reading hardware properties or handling of restart operation.
* The project `device-agent-demo` provides demo implementations that permit to test the agent and to understand how the services should be implemented. For example, `device-agent-demo` reads the temperature in a text file defined in the configuration (see below).

#### agent.raspbian
```
sensors:
  cpuTemperatureSensor:
    recordReadingsInterval: 1
    alarmConfigurations:
      -
        text: Warning alarm
        type: TemperatureAlarmWarning
        minValue: 0
        maxValue: 15
        severity: WARNING
      -
        text: Critical alarm
        type: TemperatureAlarmCritical
        minValue: 15
        maxValue: 30
        severity: CRITICAL
```
* `<sensors.cpuTemperatureSensor.recordReadingsInterval>` is the fixed rate (in seconds) at which values (e.g. cpu temperature) are being measured.
* alarmConfigurations
  * `<text>` is the text description of the alarm (e.g. Tamper sensor triggered). The measurement value can be added: "Temperature reached a value of `<value>`"
  * `<type>` identifies the type of the alarm, e.g., "com_telekom_events_TamperEvent".
  * `<minValue>` and `<maxValue>` specify the range in which the alarm will be triggered.
  * `<severity>` is the severity of the alarm: CRITICAL, MAJOR, MINOR or WARNING.

Several alarms can be defined.

#### agent.demo 
```
sensors:
  demoTemperatureSensor:
    valueFilePath: "temperatures.txt"
    recordReadingsInterval: 1
    alarmConfigurations:
      -
        text: "Warning alarm: Temperature reached a value of <value>"
        type: TemperatureAlarmWarning
        minValue: 25
        maxValue: 50
        severity: WARNING
```
* `<sensors.demoTemperatureSensor.valueFilePath>` is a text file in which temperatures will be read sequentially by the agent.

## Running

### Registering the device

* Open CoT in a web browser and go to the "Device registration" page.
* Click "Register device", enter the device ID and click "Register device".
* The device should appear with the status "WAITING FOR CONNECTION".

### Configuring the agent

* Open the `agent.yaml` file and configure following properties:
  * `agent.platformService`
  * `agent.deviceCredentialsService`
* The configuration can also be edited in the CoT.

### Starting the agent

```
/etc/init.d/cot-agent start
```

Accept the registration
* Open CoT in a web browser and go to the "Device registration" page.
* The device should appear with the status "PENDING ACCEPTANCE" and 2 buttons should be displayed: "Cancel" and "Accept".
* Click on "Accept".
* The device should now be available under "Devices"/"All devices" (with name=`agent.services.inventoryService.deviceName` and type=`agent.services.inventoryService.deviceType`)

## Development

* Developers can find further technical information in the [DEVELOPER.md](DEVELOPER.md) file.
* The [cumulocity website](https://www.cumulocity.com/guides/) also provides valuable information to understand the CoT technical concepts.

## Release Notes

Short information about what has changed between releases.

### Release 0.10.0

* Fix bug when sending measurements
* Improve update software

### Release 0.9.0

* Update software

### Release 0.8.0

* Register device
* Update inventory
* Send measurements
* Send events
* Send alarms
* Execute operations
* Update configuration