@echo off
set PROP=jmeter.properties
IF "%1" NEQ "" set PROP=%1
java -cp %CLASSPATH%;ApacheJMeter.jar org.apache.jmeter.Driver %PROP%
