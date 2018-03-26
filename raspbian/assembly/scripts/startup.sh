#!/bin/sh

# cot agent startup script

# get agent path, agent dir and agent file name
AGENT_PATH=`find / -name device-agent-raspbian-*.jar | head -n 1`
AGENT_DIR=`dirname $AGENT_PATH`
AGENT_FILE=`basename $AGENT_PATH`

# change into working dir (home of java agent)
cd $AGENT_DIR

# start agent
echo "Start CoT Java Agent at $WORKING_DIR ..."
java -Dlogback.configurationFile=./logback.xml -jar $AGENT_FILE ./agent.yaml
