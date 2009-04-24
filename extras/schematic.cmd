@echo off
cd /d %~dp0
set name=%~n1
if .%1 ==. set name=Test
call ant -f schematic.xml -Dtest=%name%
pause