@echo off

set PROP=jmeter.properties
set LOG4JCONFIG=log4j.configuration=log4j.conf
set HOST=
set PORT=

IF "%1" == "-f" set PROP=%2
IF "%1" == "-h" set HOST=-Dhttp.proxyHost=%2 -Dhttps.proxyHost=%2
IF "%1" == "-p" set PORT=-Dhttp.proxyPort=%2 -Dhttps.proxyPort=%2
IF "%3" == "-f" set PROP=%4
IF "%3" == "-h" set HOST=-Dhttp.proxyHost=%4 -Dhttps.proxyHost=%4
IF "%3" == "-p" set PORT=-Dhttp.proxyPort=%4 -Dhttps.proxyPort=%4
IF "%5" == "-f" set PROP=%6
IF "%5" == "-h" set HOST=-Dhttp.proxyHost=%6 -Dhttps.proxyHost=%6
IF "%5" == "-p" set PORT=-Dhttp.proxyPort=%6 -Dhttps.proxyPort=%6

set LOCALCLASSPATH=%CLASSPATH%

for %%i in ("..\lib\*.jar") do CALL ..\lcp %%i
for %%i in ("..\ext\*.jar") do CALL ..\lcp %%i

java -cp %LOCALCLASSPATH%;ApacheJMeter.jar -D%LOG4JCONFIG% %HOST% %PORT% org.apache.jmeter.engine.RemoteJMeterEngineImpl %PROP%
