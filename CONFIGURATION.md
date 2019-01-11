## Configuration

The agent configuration is defined in the [agent.yaml](raspbian/assembly/configuration/agent.yaml) file.

Note1: Made changes will come into effect only after a restart of the agent.

Note2: If the agent wants to communicate to the CoT via MQTT-protocol, it's neccessary that a SmartRest-template collection is already registered at the tenant (CoT). The mqtt-template-collection consists of mqtt-request-templates and mqtt-response-templates. For further information about the templates and how to administrate them take a look at the chapter [MQTT](#mqtt).
       
 

The agent.yaml file contains sections for the main components:

### agent.common
```
connectivityTimeout: -1
shutdownTimeout: 2000
```
* `<connectivityTimeout>` is the connectivity timeout configuration. On startup, the agent checks connectivity to CoT. Following values are possible:
    * -1 = no timeout, the agent keeps checking connectivity until destination reached.
    * 0 = no connectivity check
    * > 1 = timeout in minutes once the agent should shut down.
	
* `<shutdownTimeout>` is the common shutdown timeout (in milliseconds). When the agent is stopped, all services are shut down. In this case, the agent waits until all tasks from the service to stop have completed execution, or the timeout occurs, whichever happens first.

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
In case of a Raspberry Pi it will read its firmware information automatically.
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
hostName: ram.m2m.telekom.com # by default
externalIdConfig:
  valueTemplate: HARDWARE_SERIAL
  type: CoTJavaAgent
  value: null
# only one configuration can be chosen.
# configuration properties only for REST
restConfiguration:
  # proxy settings
  proxyHost: null # by default
  proxyPort: null # by default
  # the count of operations to get at one request 
  operationsRequestSize: 10
# configuration properties only for MQTT
mqttConfiguration:
  # brocker port and xID
  port: 8883 # by default
  xId: novaMqttTemplates04 # by default
  # publish and subscribe timeout in seconds
  # the minimum is 1 second
  timeout: 10 # by default
  delaySendMeasurement: 100 # milliseconds, by default
```
* `<hostName>` is the https address of your CoT instance. "https" and the tenant will be automatically added by the agent to the hostname (`url = "https://" + tenant + "." + hostname`).
* `<valueTemplate>` is the template to generate the external id value. Following values are possible:
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
* `<restConfiguration>` specifies the configuration to use the REST protocol.
    * `<proxyHost>` is the IP address or host name of the proxy server.
    * `<proxyPort>` is the port number that is used by the proxy server for client connections.
* `<mqttConfiguration>` specifies the configuration to use the MQTT protocol.
    * `<port>` is the port number that is used for MQTT connection.
    * `<xId>` represents the Id of the SmartREST Templates on the CoT.
    * `<timeout>` sets a timeout (in seconds) to publish and subscribe to a topic at the MQTT broker.
    * `<delaySendMeasurement>` is the delay (in milliseconds) between each attempt to send a measurement to the CoT. When the MQTT protocol is used, the measurements are not sent all at once but one after the other. A delay must be set in order to separate each request.

### agent.services.deviceCredentialsService
Note: The bootstrap credentials are the default values to register a new device in the CoT. Generally speaking there is no need to change anything there.

```
      # template to generate the device id (used for device registration at CoT)
      # possible values:
      #     EXTERNAL_ID_VALUE : uses the value of the external id
      #     HARDWARE_SERIAL   : uses the hardware serial
      deviceIdTemplate: EXTERNAL_ID_VALUE
      # unit is in seconds
      interval: 10
      # bootstrap credentials
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
* `<interval>` is the delay (in seconds) between each attempt to get the device credentials from the CoT.
* `bootstrapCredentials`: These bootstrap user properties are used when the agent is started and there is no device-credentials.yaml file. In this case the agent uses these properties to *register* the device in the CoT.

### agent.services.deviceService
```
handlersShutdownTimeout: 10
```
* `<handlerShutdownTimeout>` is the shutdown timeout in milliseconds for stopping device handlers.

### agent.services.measurementService
```
sendInterval: 5
```
* `<sendInterval>` is the fixed rate (in seconds) at which measurements are sent to the CoT.

### agent.services.operationService
```
interval: 10
shutdownTimeout: 5000
handlersShutdownTimeout: 500 
```
* `<interval>` is the delay (in seconds) between each attempt to handle pending operations. Once the agent has handled all pending operations, it pauses for a given amount of time (`<interval>`) before checking if there are new pending operations to handle.
* `<resultSize>` is the number of requested operations.
* `<shutdownTimeout>` and `<handlersShutdownTimeout>` are shutdown timeout in milliseconds for stopping operation service and its operation handlers.

### agent.operations
```
      testOperation:
        # in seconds
        delay: 10
      softwareUpdate:
        # possible values: MD5, SHA1, SHA256
        checksumAlgorithm: "MD5"


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
        text: "Warning alarm - Temperature reached a value of <value>"
        type: TemperatureAlarmWarning
        minValue: 60
        maxValue: 70
        severity: WARNING
      -
        text: "Critical alarm - Temperature reached a value of <value>"
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
    repeatMeasurements: true
    alarmConfigurations:
      -
        text: "Warning alarm - Temperature reached a value of <value>"
        type: TemperatureAlarmWarning
        minValue: 25
        maxValue: 50
        severity: WARNING # CRITICAL, MAJOR, MINOR, WARNING
```
* `<sensors.demoTemperatureSensor.valueFilePath>` is a text file in which temperatures will be read sequentially by the agent.

### MQTT

In order to use the agent with the MQTT protocol, the tenant must be prepared for MQTT. To set up your tenant please contact [cloudofthings@telekom.de](mailto:cloudofthings@telekom.de).

The SmartREST template collection must also be registered. It can be found in [mqtt.all.templates](platform-mqtt/src/main/resources/mqtt.all.templates).

The template collection's xID is configured in `agent.services.platformService.mqttConfiguration.xId`.

The registration of the template collection must be done by the tenant admin with a REST POST: `POST https://tenant.platform/s`. The X-ID must be specified in the first line together with the fixed ID 15. All templates to be registered then follow line by line:

```
POST /s/ HTTP/1.1
Host: <host>
Authorization: <basic auth>
X-Id: novaMqttTemplates04
Cache-Control: no-cache

15,novaMqttTemplates04
10,604,POST,/identity/globalIds/%%/externalIds,application/vnd.com.nsn.cumulocity.externalId+json,application/vnd.com.nsn.cumulocity.externalId+json,%%,STRING STRING,"{ ""type"" : ""c8y_Serial"", ""externalId"" : ""%%"" }"
10,605,PUT,/inventory/managedObjects/&&,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,UNSIGNED STRING STRING STRING STRING STRING STRING STRING STRING STRING STRING,"{""c8y_Hardware"":{""model"":""&&"",""revision"":""&&"",""serialNumber"":""&&""},""c8y_SoftwareList"":[{""name"":""Application"",""version"":""&&"",""url"":""none""},{""name"":""Bootloader"",""version"":""&&"",""url"":""none""},{""name"":""Bluetooth"",""version"":""&&"",""url"":""none""},{""name"":""Modem"",""version"":""&&"",""url"":""none""}],""c8y_Mobile"":{""imei"":""&&"",""iccid"":""&&"",""imsi"":""&&""}}"
10,606,PUT,/inventory/managedObjects/&&,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,UNSIGNED,"{""c8y_SupportedOperations"": [ ""c8y_Configuration"",""c8y_SendConfiguration"",""c8y_DownloadConfigFile"",""c8y_UploadConfigFile"",""c8y_Software"",""c8y_SoftwareList"",""c8y_Firmware"",""c8y_FirmwareList"",""c8y_SystemCommand"",""c8y_Command"",""c8y_Restart"" ] }"
10,500,GET,/devicecontrol/operations/%%,,application/vnd.com.nsn.cumulocity.operation+json,%%,UNSIGNED,
10,600,GET,/identity/externalIds/c8y_Serial/%%,,application/vnd.com.nsn.cumulocity.externalId+json,%%,STRING,
10,602,POST,/inventory/managedObjects,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,STRING STRING,"{""name"":""&&"",""type"":""&&"",""c8y_IsDevice"":{},""com_cumulocity_model_Agent"":{}}"
10,400,POST,/event/events,application/vnd.com.nsn.cumulocity.event+json,application/vnd.com.nsn.cumulocity.event+json,&&,STRING STRING STRING STRING STRING STRING,"{ ""&&"":""&&"", ""source"": { ""id"":""&&"" },""time"":""&&"",""type"":""&&"", ""text"": ""&&"" }"
10,100,POST,/measurement/measurements,application/vnd.com.nsn.cumulocity.measurement+json,application/vnd.com.nsn.cumulocity.measurement+json,&&,STRING STRING NUMBER STRING STRING STRING STRING,"{ ""&&"":{""&&"": { ""value"": &&, ""unit"": ""&&"" } }, ""time"": ""&&"",""source"": { ""id"": ""&&"" }, ""type"": ""&&"" }"
10,450,POST,/alarm/alarms,application/vnd.com.nsn.cumulocity.alarm+json,application/vnd.com.nsn.cumulocity.alarm+json,&&,STRING STRING STRING STRING STRING STRING,"{ ""type"": ""&&"", ""time"": ""&&"", ""text"": ""&&"", ""status"": ""&&"", ""severity"": ""&&"", ""source"": { ""id"": ""&&"" }, ""c8y_AgentAlarm"":""{}"" }"
10,200,GET,/devicecontrol/operations?deviceId=%%&nocache=true&status=%%&fragmentType=%%,,application/vnd.com.nsn.cumulocity.operationCollection+json,%%,UNSIGNED STRING STRING,
10,300,PUT,/devicecontrol/operations/%%,application/vnd.com.nsn.cumulocity.operation+json,application/vnd.com.nsn.cumulocity.operation+json,%%,UNSIGNED STRING STRING STRING,"{ ""status"": ""%%"" ,""c8y_OperationUpdate"": {""%%"":""%%""} }"
10,607,PUT,/inventory/managedObjects/&&,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,UNSIGNED STRING,"{""c8y_SupportedOperations"": [ ""&&"" ],""c8y_InventoryUpdate"":{""c8y_SupportedOperations"":""{}""} }"
10,610,PUT,/inventory/managedObjects/&&,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,UNSIGNED STRING STRING STRING,"{""c8y_Hardware"":{""model"":""&&"",""revision"":""&&"",""serialNumber"":""&&""},""c8y_InventoryUpdate"":{""c8y_Hardware"":""{}""}}"
10,611,PUT,/inventory/managedObjects/&&,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,UNSIGNED,"{""c8y_Hardware"":{},""c8y_InventoryUpdate"":{""c8y_Hardware"":""{}""}}"
10,613,PUT,/inventory/managedObjects/&&,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,UNSIGNED STRING STRING STRING,"{""c8y_SoftwareList"":[{""name"":""&&"",""version"":""&&"",""url"":""&&""}],""c8y_InventoryUpdate"":{""c8y_SoftwareList"":""{}""}}"
10,614,PUT,/inventory/managedObjects/&&,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,UNSIGNED,"{""c8y_SoftwareList"":{},""c8y_InventoryUpdate"":{""c8y_SoftwareList"":""{}""}}"
10,616,PUT,/inventory/managedObjects/&&,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,UNSIGNED STRING STRING STRING,"{""c8y_Mobile"":{""imei"":""&&"",""cellId"":""&&"",""iccid"":""&&""},""c8y_InventoryUpdate"":{""c8y_Mobile"":""{}""}}"
10,617,PUT,/inventory/managedObjects/&&,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,UNSIGNED,"{""c8y_Mobile"":{},""c8y_InventoryUpdate"":{""c8y_Mobile"":""{}""}}"
10,619,PUT,/inventory/managedObjects/&&,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,UNSIGNED STRING STRING STRING,"{""c8y_Firmware"":{""name"":""&&"",""version"":""&&"",""url"":""&&""},""c8y_InventoryUpdate"":{""c8y_Firmware"":""{}""}}"
10,620,PUT,/inventory/managedObjects/&&,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,UNSIGNED,"{""c8y_Firmware"":{},""c8y_InventoryUpdate"":{""c8y_Firmware"":""{}""}}"
10,622,PUT,/inventory/managedObjects/&&,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,UNSIGNED STRING,"{""c8y_Configuration"" : {""config"": ""&&""},""c8y_InventoryUpdate"":{""c8y_Configuration"":""{}""}}"
10,623,PUT,/inventory/managedObjects/&&,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,UNSIGNED,"{""c8y_Configuration"" : {},""c8y_InventoryUpdate"":{""c8y_Configuration"":""{}""}}"
11,601,,"$.externalId","$.externalId","$.managedObject.id"
11,603,,"$.c8y_IsDevice","$.id"
11,401,,@.c8y_EventStartup,$.id,@.c8y_EventStartup
11,101,,@.c8y_Temperature,$.id
11,451,,@.c8y_AgentAlarm,$.id
11,210,$.operations,"$.c8y_Restart","$.id","$.status"
11,211,$.operations,"$.c8y_TestOperation","$.id","$.status"
11,625,,"$.c8y_OperationUpdate.SUCCESSFUL","$.id"
11,626,,"$.c8y_OperationUpdate.FAILED","$.id"
11,627,,"$.c8y_OperationUpdate.EXECUTING","$.id"
11,608,,"$.c8y_InventoryUpdate.c8y_SupportedOperations","$.id"
11,612,,"$.c8y_InventoryUpdate.c8y_Hardware","$.id"
11,615,,"$.c8y_InventoryUpdate.c8y_SoftwareList","$.id"
11,618,,"$.c8y_InventoryUpdate.c8y_Mobile","$.id"
11,621,,"$.c8y_InventoryUpdate.c8y_Firmware","$.id"
11,624,,"$.c8y_InventoryUpdate.c8y_Configuration","$.id"
11,510,,@.c8y_Restart,@.id,$.status
11,511,,@.c8y_TestOperation,@.id,$.status,@.c8y_TestOperation.givenStatus
11,512,,@.c8y_Configuration,@.id,$.status,@.c8y_Configuration.config
```

The documentation of the SmartREST request and response templates can be found in the following files:
* [mqtt.request.templates](platform-mqtt/src/main/resources/templates/mqtt.request.templates)
* [mqtt.response.templates](platform-mqtt/src/main/resources/templates/mqtt.response.templates)
* [TemplateId.java](platform-mqtt/src/main/java/com/telekom/cot/device/agent/platform/mqtt/TemplateId.java)