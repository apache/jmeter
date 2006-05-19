@echo off
rem Command file to send a script to a BeanShell server

if not .%2 == . goto OK

echo Please supply:
echo.
echo P1 = port (the http port)
echo P2 = script file name
sleep 2
goto :EOF

:OK

echo Press ^C to return to
java -cp ../lib/bsh-2.0b2.jar bsh.Remote bsh://localhost:%1 %2