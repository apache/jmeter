@echo off

rem ######################################
rem # Apache JMeter Makefile for Windows #
rem ######################################

rem ########### Set your favorite java compiler and javadoc engine here ###############

set javac=jikes -O
set javadoc=javadoc -author -version

rem ########### Do not touch below here #################

set cp=xcopy /Q
set rmdir=deltree /y
set noecho=> nul
set jar=jar cf0M

echo Creating temp directory for classfiles...
%rmdir% temp %noecho%
mkdir temp

echo Building classes...
%javac% -d .\temp .\org\apache\jmeter\timers\*.java .\org\apache\jmeter\visualisers\*.java .\org\apache\jmeter\*.java %noecho%

echo Copying image files...
mkdir temp\org\apache\jmeter\images
%cp% org\apache\jmeter\images\*.* temp\org\apache\jmeter\images\*.* %noecho%

echo Copying property files...
%cp% org\apache\jmeter\*.properties temp\org\apache\jmeter\images\*.properties %noecho%

echo Creating Apache-JMeter.jar file...
%jar% ..\bin\Apache-JMeter.jar .\temp\. %noecho%

echo Cleaning javadoc directory...
%rmdir% ..\docs\api %noecho%
mkdir ..\docs\api

echo Building javadoc files...
%javadoc% -d ..\docs\api org.apache.jmeter org.apache.jmeter.timers org.apache.jmeter.visualisers %noecho%

echo Removing temp directory...
%rmdir% temp %noecho%

echo ...done

rem ######### Cleanup variables ###########
set javac=
set javadoc=
set cp=
set rmdir=
set noecho=
set jar=