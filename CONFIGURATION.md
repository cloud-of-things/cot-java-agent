## Configuration

The agent configuration is defined in the [agent.yaml](raspbian/assembly/configuration/agent.yaml) file.

Note: Made changes will come into effect only after a restart of the agent.

The agent.yaml file contains sections for the main components:

### agent.common
```
connectivityTimeout: -1
shutdownTimeout: 2000
```
* `<connectivityTimeout>` is the connectivity timeout configuration. On startup, the agent checks connectivity to CoT. Following values are possible:
	* -1 = no timeout, the agent keeps checking connectivity until destination reached.
	* 0 = no connectivity check
	* \> 1 = timeout in minutes once the agent should shut down.
* `<shutdownTimeout>` is the common shutdown timeout in milliseconds. When the agent is stopped, all services are shut down. In this case, the agent waits until all tasks from the service to stop have completed execution, or the timeout occurs, whichever happens first.

### agent.properties
```
system:
  hardware:
    model:  "RaspPi BCM1234"
    revision: 1234
    serialNumber: 0000000012345678
  firmware:
    name: "Linux raspberrypi"
    version: "4.5.67-v8+"
    url: "http://loadnewversion.de/"
  mobile:
    imei: "861145012345678"
    cellId: "4567-A456"
    iccid: "89490200001234567890"
```
* `system.hardware` contains basic hardware information for a device, such as model and serial number. This information is used to update the fragment `c8y_Hardware` in the inventory when the agent is started.  
In case of the demo agent you have to set the hardware information. Other devices will read their hardware information automatically.
* `system.firmware` contains information on a device's firmware. This information is used to update the fragment `c8y_Firmware` in the inventory when the agent is started.   
In case of the demo agent you have to set the firmware information. Other devices will read their firmware information automatically. 
* `system.mobile` holds basic connectivity-related information, such as the equipment identifier of the modem (IMEI) in the device. This information is used to update the fragment `c8y_Mobile` in the inventory when the agent is started.  

### agent.services.inventoryService
```
deviceType: c8y_Linux
deviceName: CoTJavaAgent
isDevice: true
isAgent: true
```
These inventory properties are used in order to create the device in the inventory.
* A managed object representing the device is created (with the properties `<deviceName>` and `<deviceType>`).
* `<isDevice>`: if true, the fragment `c8y_IsDevice` is added in the inventory when the agent is started, the device is visible in the CoT and the agent is able to send measurements.
* `<isAgent>`: if true, the fragment `com_cumulocity_model_Agent` is added in the inventory when the agent is started, and the agent is able to execute operations.

### agent.services.platformService
```
proxyHost: null # by default
proxyPort: null # by default
hostName: ram.m2m.telekom.com # by default
externalIdConfig:
  valueTemplate: HARDWARE_SERIAL
  type: CoTJavaAgent
  value: null
```
* `<proxyHost>` is the IP address or host name of the proxy server.
* `<proxyPort>` is the port number that is used by the proxy server for client connections.
* `<hostName>` is the https address of your CoT instance. "https" and the tenant will be automatically added by the agent to the hostname (`url = "https://" + tenant + "." + hostname`).
* `<valueTemplate>` is the template to generate the external id value. Possible values: NO_TEMPLATE, HARDWARE_SERIAL, TYPE_HARDWARE_SERIAL
	* NO_TEMPLATE  
	  * `"value"` is used
	  * `"value"` must not be **null**
	* HARDWARE_SERIAL
	  * uses the serial number of the Raspberry Pi, read from the hardware automatically by the agent 
	  * `"value"` must be **null**
	* TYPE_HARDWARE_SERIAL
	  * uses the value of `"type"` + serial number  
	  **Example:** *CoTJavaAgent-0000000012345678*
	  * `"value"` must be **null**  
		(a combination of external id type (agent.services.platformService.externalIdConfig.type) and hardware serial number (agent.properties.system.hardware.serialNumber) is used (e.g. CoTJavaAgent-0000000012345678))

### agent.services.deviceCredentialsService
Note: The bootstrap credentials are the default values to register a new device in the CoT. Generally speaking there is no need to change anything there.
```
deviceIdTemplate: EXTERNAL_ID_VALUE
interval: 10
bootstrapCredentials:
  username: devicebootstrap
  password: Fhdt1bb1f
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
    recordReadingsInterval: 1 # interval in seconds at which values are read
    alarmConfigurations:
      -
        text: "Warning alarm: Temperature reached a value of <value>"
        type: TemperatureAlarmWarning
        minValue: 60
        maxValue: 70
        severity: WARNING
      -
        text: "Critical alarm: Temperature reached a value of <value>"
        type: TemperatureAlarmCritical
        minValue: 70
        maxValue: 100
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
    recordReadingsInterval: 1 # interval in seconds at which values are read
    alarmConfigurations:
      -
        text: "Warning alarm: Temperature reached a value of <value>"
        type: TemperatureAlarmWarning
        minValue: 25
        maxValue: 50
        severity: WARNING
```
* `<sensors.demoTemperatureSensor.valueFilePath>` is a text file in which temperatures will be read sequentially by the agent.