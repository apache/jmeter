@echo off

::
:: Win32 build script for ApacheJMeter.jar
::
:: @author Stefano Mazzocchi <stefano@apache.org>
:: @version $Revision$ $Date$
::

echo Creating ApacheJMeter.jar...
echo  * Compiling Java source...
md .\temp
javac -O -d .\temp .\org\apache\jmeter\timers\*.java .\org\apache\jmeter\threads\*.java .\org\apache\jmeter\controllers\*.java .\org\apache\jmeter\visualizers\*.java .\org\apache\jmeter\samplers\*.java .\org\apache\jmeter\*.java

echo  * Copying image files...
md temp\org\apache\jmeter\images
copy .\org\apache\jmeter\images\*.* .\temp\org\apache\jmeter\images\*.* >nul

echo  * Copying property files...
copy .\org\apache\jmeter\*.properties .\temp\org\apache\jmeter\*.properties >nul

echo  * Creating ApacheJMeter.jar file in ..\bin ...
cd .\temp
jar cmf0 ..\MANIFEST ..\..\bin\ApacheJMeter.jar *.* >nul
cd ..

echo  * Removing temp directory...
rem windows 9x
deltree /y temp >nul
rem windows nt/2000
del /F /S /Q temp >nul

echo Done.
