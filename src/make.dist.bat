@echo off

::
:: Win32 build script for Apache JMeter distribution (not tested under NT, may not work)
::
:: @author Stefano Mazzocchi <stefano@apache.org>
:: @version $Revision$ $Date$
::

:: Set package version
if "%name%"=="" set name=Apache_JMeter

:: Set package version
if "%version%"=="" set version=1.4

:: Set temp directory name
if "%dir%"=="" set dir="temp"

:: Set local directory name
if "%cwd%"=="" set cwd="."

echo.
echo Creating %name%_%version%.jar...
echo.

:: creating the tree
echo  * creating the tree
cd ..
md %dir%
copy .\%cwd%\README .\%dir%\readme.txt > nul
copy .\%cwd%\LICENSE .\%dir%\license.txt > nul
copy .\%cwd%\index.html .\%dir%\index.html > nul
xcopy32 /S .\%cwd%\bin\ .\%dir%\bin\ > nul
xcopy32 /S .\%cwd%\docs\ .\%dir%\docs\ > nul
xcopy32 /S .\%cwd%\src\ .\%dir%\src\ > nul
rem del .\%dir%\bin\jmeter > nul
rem del .\%dir%\src\Makefile > nul
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

:: jar it
echo  * compressing the package (jar)
jar -cf ..\%name%_%version%.jar *.*
if errorlevel 1 goto fatal

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
set cwd=
