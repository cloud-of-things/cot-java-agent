set configurationFile=agent.yaml
if "%1"=="" goto findAgentDir
set configurationFile=%1

:findAgentDir
for /f %%i in ('dir /B device-agent-demo-*') do set agentJar=%%i
java -Dlogback.configurationFile=./logback.xml -jar %agentJar% %configurationFile%
exit