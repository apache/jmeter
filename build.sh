#!/bin/sh

##   $Id$
##   Copyright 2001-2004 The Apache Software Foundation
## 
##   Licensed under the Apache License, Version 2.0 (the "License");
##   you may not use this file except in compliance with the License.
##   You may obtain a copy of the License at
## 
##       http://www.apache.org/licenses/LICENSE-2.0
## 
##   Unless required by applicable law or agreed to in writing, software
##   distributed under the License is distributed on an "AS IS" BASIS,
##   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
##   See the License for the specific language governing permissions and
##   limitations under the License.

#--------------------------------------------
# No need to edit anything past here
#--------------------------------------------
if test -z "$JAVA_HOME" ; then
    echo "ERROR: JAVA_HOME not found in your environment."
    echo "Please, set the JAVA_HOME variable in your environment to match the"
    echo "location of the Java Virtual Machine you want to use."
    exit
fi

if test -z "$ANT_HOME" ; then
    echo "ERROR: ANT_HOME not found in your environment."
    exit
fi

if test -f $JAVA_HOME/lib/tools.jar ; then
    CLASSPATH=$CLASSPATH:${JAVA_HOME}/lib/tools.jar
fi

CLASSPATH=$CLASSPATH:${ANT_HOME}/lib/ant.jar

# convert the existing path to unix
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

## The build.xml file now sets up the required classpath

## for i in ./lib/*.jar
## do
##     if [ i != "./lib/jorphan.jar" ]
## 	then
## 		CLASSPATH=$CLASSPATH:$i
## 	fi
## done

## for i in ../jakarta-site2/lib/*.jar
## do
##     CLASSPATH=$CLASSPATH:$i
## done

# convert the unix path to windows
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

BUILDFILE=build.xml
echo $JAVA_HOME/bin/java -classpath $CLASSPATH org.apache.tools.ant.Main -buildfile $BUILDFILE "$@"

$JAVA_HOME/bin/java -classpath $CLASSPATH org.apache.tools.ant.Main \
                      -buildfile $BUILDFILE "$@"