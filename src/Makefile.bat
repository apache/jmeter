@echo off

rem ########### Set your favorite java compiler and javadoc engine here ###############
set javac=javac -O
set javadoc=javadoc -author -version

rem ########### Do not touch below here #################

echo Compiling Apache JMeter...
%javac% -d .\temp .\org\apache\jmeter\timers\*.java .\org\apache\jmeter\visualisers\*.java .\org\apache\jmeter\*.java

xcopy /Q .\org\apache\jmeter\jmeter.properties .\temp\org\apache\jmeter\.
xcopy /Q .\org\apache\jmeter\images\*.* .\temp\org\apache\jmeter\images\*.*

echo building jar archive...
cd .\temp
jar cf0 Apache-JMeter.jar .

xcopy /Q .\Apache-JMeter.jar ..\..\bin\*.jar
cd ..
deltree /Y .\temp

echo building javadoc...
%javadoc% -d ..\docs\api org.apache.jmeter org.apache.jmeter.timers org.apache.jmeter.visualisers

echo done.