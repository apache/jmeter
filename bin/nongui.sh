#!/bin/sh
USAGE=$0" [-h proxyHost] [-p proxyPort] [-f property-file] [-o jmeter script file]"
LOG4JCONFIG=log4j.configuration=log4j.conf

PROPERTIES=`dirname $0`/jmeter.properties

while getopts h:p:f:o: x; do
	 case $x in
	h) JAVA_PROPS=$JAVA_PROPS" -Dhttp.proxyHost="$OPTARG" -Dhttps.proxyHost="$OPTARG;;
	p) JAVA_PROPS=$JAVA_PROPS" -Dhttp.proxyPort="$OPTARG" -Dhttps.proxyPort="$OPTARG;;
	f) PROPERTIES=$OPTARG;;
	o) SRCFILE=$OPTARG;;
	?) echo $USAGE; exit -1;;
	 esac;
done

for i in `dirname $0`/../lib/*.jar
do
    CLASSPATH=${CLASSPATH}:$i
done

for i in `dirname $0`/../ext/*.jar
do
    CLASSPATH=${CLASSPATH}:$i
done

java -classpath $CLASSPATH:`dirname $0`/ApacheJMeter.jar $JAVA_PROPS -D$LOG4JCONFIG org.apache.jmeter.NewDriver $PROPERTIES $SRCFILE
