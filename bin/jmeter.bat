@echo off

set java=javaw

%java% -classpath %classpath%;Apache-JMeter.jar org.apache.jmeter.JMeter

set java=