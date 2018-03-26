#!/bin/bash

LOGFILE=./update.log

if [ "$(id -u)" != "0" ]; then
  ROOT=false
else
  ROOT=true
fi


echo "Start agent update" > $LOGFILE

#stop agent
if [ "$ROOT" = true ]; then
    /etc/init.d/cot-java-agent stop >> $LOGFILE 2>&1
else
    sudo /etc/init.d/cot-java-agent stop >> $LOGFILE 2>&1
fi
sleep 15

#set variables for installation
PACKAGE_PATH=`find . -name device-agent*.deb | head -n 1`
PACKAGE_FILE=`basename $PACKAGE_PATH`

#install
echo "Install $PACKAGE_FILE..." >> $LOGFILE
if [ "$ROOT" = true ]; then
        dpkg -i $PACKAGE_FILE >> $LOGFILE 2>&1
else
        sudo dpkg -i $PACKAGE_FILE >> $LOGFILE 2>&1
fi

#copy saved agent.yaml to new version
echo "Restore agent configuration" >> $LOGFILE
cp ./agent.yaml /opt/cot-java-agent

#start agent
if [ "$ROOT" = true ]; then
    /etc/init.d/cot-java-agent start >> $LOGFILE 2>&1
else
    sudo /etc/init.d/cot-java-agent start >> $LOGFILE 2>&1
fi
echo "Agent update done" >> $LOGFILE
