@echo off

::
:: Win32 build script for Apache JMeter zip distribution
::
:: @author Stefano Mazzocchi <stefano@apache.org>
:: @version $Revision$ $Date$
::

:: Set package version
if "%name%"=="" set name=Apache_JMeter

:: Set package version
if "%version%"=="" set version=1.0

:: Set directory name
if "%dir%"=="" set dir="temp"

:: Set the zip archiver
if "%zip%"=="" set zip=pkzip25

echo.
echo Creating %name%_%version%.zip...
echo.

:: creating the tree
echo  * creating the tree
cd ..
md %dir%
copy .\README .\%dir%\readme.txt > nul
copy .\LICENSE .\%dir%\license.txt > nul
xcopy32 /S .\bin\ .\%dir%\bin\ > nul
xcopy32 /S .\docs\ .\%dir%\docs\ > nul
xcopy32 /S .\src\ .\%dir%\src\ > nul
del .\%dir%\bin\jmeter > nul
del .\%dir%\src\Makefile > nul
del .\%dir%\src\make.dist.bat > nul
cd %dir%

:: compile the package
echo  * compiling the package...
echo.
cd src
call make.bat > nul
cd ..

:: create the docs
echo  * creating the docs...
echo.
cd docs\api
call make.docs.bat > nul
cd ..\..

:: remove unused files
:: Note: sweep is a tool that repeats the same bat command on every
::       directory recursively. It may be found in the CVS module 
::         /jserv/src/build/package/zip/tools
echo  * remove unused files...
sweep deltree /y CVS > nul

:: zip it
echo  * compressing the package
%zip% -add -rec -dir -max -silent ..\%name%_%version%.zip *.*
if errorlevel 1 goto fatal
goto done

:fatal
echo Some error occurred in the script. Package creation aborted.
goto end

:done
echo.
echo Done.

:end
cd ..
deltree /y %dir% > nul

set name=
set version=
set dir=
set zip=
