@echo off


rem   Licensed to the Apache Software Foundation (ASF) under one or more
rem   contributor license agreements.  See the NOTICE file distributed with
rem   this work for additional information regarding copyright ownership.
rem   The ASF licenses this file to You under the Apache License, Version 2.0
rem   (the "License"); you may not use this file except in compliance with
rem   the License.  You may obtain a copy of the License at
rem 
rem       http://www.apache.org/licenses/LICENSE-2.0
rem 
rem   Unless required by applicable law or agreed to in writing, software
rem   distributed under the License is distributed on an "AS IS" BASIS,
rem   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem   See the License for the specific language governing permissions and
rem   limitations under the License.

rem Generate temporary certificate for use with JMeter Proxy recorder
rem Usage:  proxycert [validity(default 1)]
rem e.g. proxycert 7

setlocal

set KEYSTORE=proxyserver.jks
if not exist %KEYSTORE% goto NOTEXISTS
echo %KEYSTORE% exists; please rename or delete it before creating a replacement
goto :EOF
:NOTEXISTS

set DNAME="cn=JMeter Proxy (DO NOT TRUST)"

set VALIDITY=1
if not .%1 == . set VALIDITY=%1

rem Must agree with property proxy.cert.keystorepass
set STOREPASSWORD=password
rem Must agree with proxy.cert.keypassword
set KEYPASSWORD=password

rem generate the keystore with the certificate
keytool -genkeypair -alias jmeter -keystore %KEYSTORE% -keypass %KEYPASSWORD% -storepass %STOREPASSWORD% -validity %VALIDITY% -keyalg RSA -dname %DNAME%

rem show the contents
keytool -list -v -keystore %KEYSTORE% -storepass %STOREPASSWORD%
