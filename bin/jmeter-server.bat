set PROP=jmeter.properties
set HOST=
set PORT=
IF "%1" EQU "-f" set PROP=%2
IF "%1" EQU "-h" set HOST=-Dhttp.proxyHost=%2
IF "%1" EQU "-p" set PORT=-Dhttp.proxyPort=%2
IF "%3" EQU "-f" set PROP=%4
IF "%3" EQU "-h" set HOST=-Dhttp.proxyHost=%4
IF "%3" EQU "-p" set PORT=-Dhttp.proxyPort=%4
IF "%5" EQU "-f" set PROP=%6
IF "%5" EQU "-h" set HOST=-Dhttp.proxyHost=%6
IF "%5" EQU "-p" set PORT=-Dhttp.proxyPort=%6
c:\jbuilder4\jdk1.3\jre\bin\java -cp ../lib/xerces.jar;c:\development\compiled\jmeter %HOST% %PORT% org.apache.jmeter.engine.RemoteJMeterEngineImpl