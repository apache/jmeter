echo off

set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar

for %%i in (".\lib\*.jar") do CALL lcp %%i
echo %LOCALCLASSPATH%

for %%i in (..\jakarta-site2\lib\*.jar) do CALL lcp %%i

SET BUILDFILE=build.xml
echo %LOCALCLASSPATH%
java -classpath %LOCALCLASSPATH% org.apache.tools.ant.Main -buildfile %BUILDFILE% %1
