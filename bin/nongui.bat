@echo on
set PROP=jmeter.properties
rem set the log4j configuration file 
set LOG4JCONFIG=log4j.configuration=log4j.conf

IF "%1" EQU "-f" set PROP=%2
IF "%1" EQU "-h" set HOST=-Dhttp.proxyHost=%2 -Dhttps.proxyHost=%2
IF "%1" EQU "-p" set PORT=-Dhttp.proxyPort=%2 -Dhttps.proxyPort=%2
IF "%1" EQU "-o" set SRC=%2

IF "%3" EQU "-f" set PROP=%4
IF "%3" EQU "-h" set HOST=-Dhttp.proxyHost=%4 -Dhttps.proxyHost=%4
IF "%3" EQU "-p" set PORT=-Dhttp.proxyPort=%4 -Dhttps.proxyPort=%4
IF "%3" EQU "-o" set SRC=%4

IF "%5" EQU "-f" set PROP=%6
IF "%5" EQU "-h" set HOST=-Dhttp.proxyHost=%6 -Dhttps.proxyHost=%6
IF "%5" EQU "-p" set PORT=-Dhttp.proxyPort=%6 -Dhttps.proxyPort=%6
IF "%5" EQU "-o" set SRC=%6

IF "%7" EQU "-f" set PROP=%8
IF "%7" EQU "-h" set HOST=-Dhttp.proxyHost=%8 -Dhttps.proxyHost=%8
IF "%7" EQU "-p" set PORT=-Dhttp.proxyPort=%8 -Dhttps.proxyPort=%8
IF "%7" EQU "-o" set SRC=%8

set CLSPATH=../lib/xerces.jar;ApacheJMeter.jar;../lib/Tidy.jar;../lib/log4j.jar;
java -cp %CLASSPATH%;%CLSPATH% -D%LOG4JCONFIG% %HOST% %PORT% org.apache.jmeter.NonGuiDriver %PROP% %SRC%
