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

## This is a simple wrapper for the script bin/jmeter.sh
##
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

# Make sure prerequisite environment variables are set
if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
  if [ "`uname`" = "Darwin" ]; then
    #
    if [ -x '/usr/libexec/java_home' ] ; then
      export JAVA_HOME=`/usr/libexec/java_home`
    #
    elif [ -d "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home" ]; then
      export JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home"
    fi
  else
    JAVA_PATH=`which java 2>/dev/null`
    if [ "x$JAVA_PATH" != "x" ]; then
      JAVA_PATH=`dirname $JAVA_PATH 2>/dev/null`
      JRE_HOME=`dirname $JAVA_PATH 2>/dev/null`
    fi
    if [ "x$JRE_HOME" = "x" ]; then
      # XXX: Should we try other locations?
      if [ -x /usr/bin/java ]; then
        JRE_HOME=/usr
      fi
    fi
  fi
  if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
    echo "Neither the JAVA_HOME nor the JRE_HOME environment variable is defined"
    echo "At least one of these environment variable is needed to run this program"
    exit 1
  fi
fi
if [ -z "$JAVA_HOME" -a "$1" = "debug" ]; then
  echo "JAVA_HOME should point to a JDK in order to run in debug mode."
  exit 1
fi
if [ -z "$JRE_HOME" ]; then
  JRE_HOME="$JAVA_HOME"
fi
if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME="$JRE_HOME"
fi

#--add-opens if JAVA 9
JAVA9_OPTS=

# Minimal version to run JMeter
MINIMAL_VERSION=8

# Check if version is from OpenJDK or Oracle Hotspot JVM prior to 9 containing 1.${version}.x
CURRENT_VERSION=`"${JAVA_HOME}/bin/java" -version 2>&1 | awk -F'"' '/version/ {gsub("^1[.]", "", $2); gsub("[^0-9].*$", "", $2); print $2}'`

# Check if Java is present and the minimal version requirement
if [ "$CURRENT_VERSION" -gt "$MINIMAL_VERSION" ]; then
    JAVA9_OPTS="--add-opens java.desktop/sun.awt=ALL-UNNAMED --add-opens java.desktop/javax.swing.text.html=ALL-UNNAMED --add-opens java.desktop/sun.swing=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED"
fi

# Don't add additional arguments to the JVM start, except those needed for Java 9
JMETER_COMPLETE_ARGS=true

# add the Java9 args before the user given ones
JVM_ARGS="$JAVA9_OPTS $JVM_ARGS"
export JVM_ARGS JMETER_COMPLETE_ARGS

"${PRGDIR}/jmeter" "$@"
