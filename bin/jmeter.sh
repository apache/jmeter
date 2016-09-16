#! /bin/sh

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

## Basic JMeter startup script for Un*x systems
## See the "jmeter" script for details of options that can be used for Sun JVMs

##   ==============================================
##   Environment variables:
##   JVM_ARGS - optional java args, e.g. -Dprop=val
##
##   e.g.
##   JVM_ARGS="-Xms512m -Xmx512m" jmeter.sh etc.
##
##   ==============================================

# Minimal version to run JMeter
MINIMAL_VERSION=1.7.0

# Check if Java is present and the minimal version requirement
_java=`type java | awk '{ print $ NF }'`
CURRENT_VERSION=`"$_java" -version 2>&1 | awk -F'"' '/version/ {print $2}'`
minimal_version=`echo $MINIMAL_VERSION | awk -F'.' '{ print $2 }'`
current_version=`echo $CURRENT_VERSION | awk -F'.' '{ print $2 }'`
if [ $current_version ]; then
        if [ $current_version -lt $minimal_version ]; then
                 echo "Error: Java version is too low to run JMeter. Needs at least Java >= ${MINIMAL_VERSION}." 
                 exit 1
        fi
    else
         echo "Not able to find Java executable or version. Please check your Java installation."
         exit 1
fi

JMETER_OPTS=""
case `uname` in
   Darwin*)
   # Add Mac-specific property - should be ignored elsewhere (Bug 47064)
   JMETER_OPTS="-Xdock:name=JMeter -Xdock:icon="`dirname $0`/../docs/images/jmeter_square.png" -Dapple.laf.useScreenMenuBar=true -Dapple.eawt.quitStrategy=CLOSE_ALL_WINDOWS"
   ;;
esac


# resolve links - $0 may be a softlink (code as used by Tomcat)
# N.B. readlink would be a lot simpler but is not supported on Solaris
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`

java $JVM_ARGS $JMETER_OPTS -jar "$PRGDIR/ApacheJMeter.jar" "$@"
