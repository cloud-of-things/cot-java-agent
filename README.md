# Cloud of Things Java Reference Agent

The Cloud of Things (CoT) Java Reference Agent described in this document can be used as basic implementation for customer CoT Java Agents, e.g. for devices like Raspberry Pi or customer specific devices.

The [Cloud of Things](https://iot.telekom.com/en/platforms/cloud-of-things/) (German Cloud der Dinge) is a platform for the Internet of Things by T-Systems International GmbH. Inside this repository you will find a Java based Reference Agent.

This Java agent provides a reference implementation for device management features of the Cloud of Things as well as a library for the Raspberry Pi.

The Java agent contains following functionalities:
* Register device
* Update inventory
* Send measurements
* Send events
* Send alarms
* Execute operations
* Update configuration
* Update software (only available using HTTP REST communication)

_Current version is: 0.18.1_

## Prerequisites  

  An installation of Java SE 8 is required. To verify the availability of Java on your system, start a terminal and type:

```
$ java -version
java version "1.8.x_xxx"
```

To install Java visit http://java.com.   

Note: Raspbian, the debian based, official operating system for all models of the Raspberry Pi already contains Java SE. 

## Installing

Software packages are available at https://github.com/cloud-of-things/cot-java-agent/releases.  
To install the agent on a Raspberry Pi, start the terminal and install the pre-built debian package.

```
$ wget https://github.com/cloud-of-things/cot-java-agent/releases/download/v0.18.0/device-agent-raspbian_0.18.0_all.deb
$ sudo dpkg -i device-agent-raspbian_0.18.0_all.deb
```
After installation, the agent software can be found at directory `/opt/cot-java-agent/` on the Raspberry Pi.  

Note: The recommended model is "Raspberry Pi 3 Model B" or later.																	 

## Configuration

The agent is completely configured via the single, comprehensive file  [agent.yaml](raspbian/assembly/configuration/agent.yaml)  

To edit the agent.yaml use a text editor (e.g. Vi or nano):

				
```
$ sudo nano /opt/cot-java-agent/agent.yaml
```
### deviceName
To set the name for the initial creation of the device in the CoT inventory adapt the deviceName:
```
agent:
...
  services:
    ...
    inventoryService:
      deviceName: CoTJavaAgent
```

### platformService

You can choose between two different ways to communicate with CoT
* HTTP REST
* MQTT

They are both configured in a different way. Take a look at one of the following sections.

#### restConfiguration

Delete the complete mqttConfiguration body.  
**Only** if the Raspberry Pi is connected to the internet via a proxy server add the proxy details (e.g. proxyHost = 10.11.12.13, proxyPort = 3128).  
**Only** if the hostName of your CoT instance differs from the default value (representing the standard production environment) adapt the hostName.   
_Note: hostName is the https address of your CoT instance. "https" and the tenant will be automatically added by the agent to the hostname (url = "https://" + tenant + "." + hostname)._

```
agent:
...
  services:
    ...
    platformService:
      hostName:   ram.m2m.telekom.com # by default
      ...
      restConfiguration:
        proxyHost:  null # by default
        proxyPort:  null # by default
        operationsRequestSize: 10 # by default
```
#### mqttConfiguration

Delete the complete restConfiguration body.  
Add "nb-iot" in front of your hostName.  
**Only** if the hostname (without "nb-iot") of your CoT instance differs from the default value (representing the standard production environment) adapt the hostName.   
_Note: hostName is the address of your CoT instance. The tenant will be automatically added by the agent to the hostname (url = "tenant + "." + hostname)._  

```
agent:
...
  services:
    ...
    platformService:
      hostName: nb-iot.ram.m2m.telekom.com 
      ...
      mqttConfiguration:
        port: 8883 # by default
        xId: novaMqttTemplates04 # by default
        timeout: 10 # by default
```

The tenant must be prepared for MQTT and the SmartREST template collection (novaMqttTemplates04) must be registered. See details in [CONFIGURATION.md](CONFIGURATION.md).

For detailed information about the various parameters for the configuration via the `agent.yaml` have a look at [CONFIGURATION.md](CONFIGURATION.md)

## Running

### Starting the agent
```
$ sudo /etc/init.d/cot-java-agent start
```	

### Registering the device

Get the serial number of your Raspberry Pi:
```
$ cat /proc/cpuinfo | grep Serial
Serial          : 0000000012345678
```	
* Open your CoT account in a web browser and go to the "Device registration" page
* Click "Register device", enter the serial number and click "Register device"

Accept the registration
* The device should appear with the status "PENDING ACCEPTANCE"
* Approve the device by clicking on "Accept"
* The device should now be available under "Devices"/"All devices" 

### Sending measurements and alarms	

The default implementation for the Raspberry Pi will:
* measure the CPU temperature every second
* send these measurements to the CoT every five seconds
* raise and send alarms if limits are exceeded

Note: The values can be changed by editing the agent.yaml (on the device) or via configuration update (from the CoT). Please be aware that made changes will come into effect only after a restart of the agent.

```
agent:
...
  services:
    ...
    sensorService:
      sendInterval: 5 # interval in seconds at which measurements are sent to the CoT
    ...  
  raspbian:
    sensors:
      cpuTemperatureSensor:
        recordReadingsInterval: 1 # interval in seconds at which values are read
        alarmConfigurations:
          -
            text: "Warning alarm - Temperature reached a value of <value>"
            type: TemperatureAlarmWarning
            minValue: 60
            maxValue: 70
            severity: WARNING # CRITICAL, MAJOR, MINOR, WARNING
          -
            text: "Critical alarm - Temperature reached a value of <value>"
            type: TemperatureAlarmCritical
            minValue: 70
            maxValue: 100
            severity: CRITICAL # CRITICAL, MAJOR, MINOR, WARNING
```

### Stopping the agent

```
$ sudo /etc/init.d/cot-java-agent stop
```
### Status of the agent

```
$ sudo /etc/init.d/cot-java-agent status
```

## Development

Developers can find further technical information in the [DEVELOPER.md](DEVELOPER.md) file.

## Release Notes

Short information about what has changed between releases.

### Release 0.18.1

* Updated CoT MQTT SDK to version 1.0.1

### Release 0.18.0

* Fix bug in setting operation status
* Support comments in configuration file

### Release 0.17.0

* Send alarms with MQTT
* Execute inventory update with MQTT
* Execute operations with MQTT
* Execute configuration update with MQTT

### Release 0.12.0

* Register device with MQTT
* Send measurements with MQTT
* Send events with MQTT

### Release 0.10.2

* Documentation revised
* Connectivity check at startup added

### Release 0.10.1

* Fix CPU temperature bug

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