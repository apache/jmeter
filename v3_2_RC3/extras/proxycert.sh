#!/bin/sh

##   Licensed to the Apache Software Foundation (ASF) under one or more
##   contributor license agreements.  See the NOTICE file distributed with
##   this work for additional information regarding copyright ownership.
##   The ASF licenses this file to You under the Apache License, Version 2.0
##   (the "License"); you may not use this file except in compliance with
##   the License.  You may obtain a copy of the License at
## 
##       http://www.apache.org/licenses/LICENSE-2.0
## 
##   Unless required by applicable law or agreed to in writing, software
##   distributed under the License is distributed on an "AS IS" BASIS,
##   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
##   See the License for the specific language governing permissions and
##   limitations under the License.


## Generate temporary proxyserver for use with JMeter Proxy recorder
## Usage:  sh proxycert.sh [validity(default 1)]
## e.g. sh proxyxcert.sh 7

KEYSTORE=proxyserver.jks
if [ -r ${KEYSTORE} ]
then
    echo "${KEYSTORE} exists; please rename or delete it before creating a replacement"
    exit 1
fi

DNAME="cn=JMeter Proxy (DO NOT TRUST)"
VALIDITY=${1:-1}
# Must agree with property proxy.cert.keystorepass
STOREPASSWORD=password
# Must agree with proxy.cert.keypassword
KEYPASSWORD=password

## generate the keystore with the certificate
keytool -genkeypair -alias jmeter -keystore ${KEYSTORE} -keypass ${KEYPASSWORD} -storepass ${STOREPASSWORD} -validity ${VALIDITY} -keyalg RSA -dname "${DNAME}"

## show the contents
keytool -list -v -keystore ${KEYSTORE} -storepass ${STOREPASSWORD}
