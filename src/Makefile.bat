@echo off

set repository=c:\java\classes
set compiler=jikes -O -d %repository%

echo Compiling Apache JMeter...
%compiler% .\org\apache\jmeter\timers\*.java .\org\apache\jmeter\visualisers\*.java .\org\apache\jmeter\*.java

xcopy /S %repository%\org\apache\jmeter\*.class ..\temp\org\apache\jmeter\*.class
xcopy .\org\apache\jmeter\jmeter.properties ..\temp\org\apache\jmeter\.
xcopy .\org\apache\jmeter\images\*.* ..\temp\org\apache\jmeter\images\*.*

echo building jar archive...
cd ..\temp
jar cvf0 Apache-JMeter.jar .

xcopy .\Apache-JMeter.jar ..\bin\*.jar
deltree /Y ..\temp
echo ...all done