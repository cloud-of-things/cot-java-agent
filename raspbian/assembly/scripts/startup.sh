#!/bin/sh

# cot agent startup script

# use default configuration file or as parameter #1 given file
CONFIGURATION_FILE=./agent.yaml
if [ "$1" != "" ]; then
	CONFIGURATION_FILE=$1
fi

# get agent path, agent dir and agent file name
AGENT_PATH=`find / -name device-agent-raspbian-*.jar | head -n 1`
AGENT_DIR=`dirname $AGENT_PATH`
AGENT_FILE=`basename $AGENT_PATH`

# change into working dir (home of java agent)
cd $AGENT_DIR

# start agent
echo "Start CoT Java Agent at $AGENT_DIR, configuration = $CONFIGURATION_FILE ..."
java -Dlogback.configurationFile=./logback.xml -jar $AGENT_FILE $CONFIGURATION_FILE
