echo on
set CLASSPATH=%JMETER_HOME%\lib\ext\ApacheJMeter_core.jar;%JMETER_HOME%\lib\jorphan.jar;%JMETER_HOME%\lib\logkit-1.0.1.jar
START rmiregistry
jmeter -s
