<h1 align="center"><img src="https://jmeter.apache.org/images/logo.svg" alt="Apache JMeter logo" /></h1>
<h4 align="center">Open Source application designed to load test applications and measure performance. By The Apache Software Foundation</h4>
<br>

[![Build Status](https://api.travis-ci.org/apache/jmeter.svg?branch=master)](https://travis-ci.org/apache/jmeter/)
[![codecov](https://codecov.io/gh/apache/jmeter/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/jmeter)
[![License](https://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Stack Overflow](https://img.shields.io/:stack%20overflow-jmeter-brightgreen.svg)](https://stackoverflow.com/questions/tagged/jmeter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.jmeter/ApacheJMeter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.jmeter/ApacheJMeter)
[![Javadocs](https://www.javadoc.io/badge/org.apache.jmeter/ApacheJMeter_core.svg)](https://www.javadoc.io/doc/org.apache.jmeter/ApacheJMeter_core)
[![Twitter](https://img.shields.io/twitter/url/https/github.com/apache/jmeter.svg?style=social)](https://twitter.com/intent/tweet?text=Powerful%20load%20testing%20with%20Apache%20JMeter:&url=https://jmeter.apache.org)

## What is it

Apache JMeter may be used to test performance both on static and dynamic resources, Web dynamic applications.
It can be used to simulate a heavy load on a server, group of servers, network or object to test its strength or to analyze overall performance under different load types.

![Image of JMeter 4.0](https://raw.githubusercontent.com/apache/jmeter/master/xdocs/images/screenshots/JMETER_4.0.png)

Apache JMeter features include:

Ability to load and performance test many different applications/server/protocol types:

- Web - HTTP, HTTPS (Java, NodeJS, PHP, ASP.NET,...)
- SOAP / REST Webservices
- FTP
- Database via JDBC
- Neo4j Database via Bolt and Cypher
- LDAP
- Message-oriented Middleware (MOM) via JMS
- Mail - SMTP(S), POP3(S) and IMAP(S)
- Native commands or shell scripts
- TCP
- Java Objects

Full featured Test IDE that allows fast Test Plan **recording (from Browsers or native applications), building and debugging.**

[**Command-line mode (Non GUI / headless mode)**](http://jmeter.apache.org/usermanual/get-started.html#non_gui) to load test from any Java compatible OS (Linux, Windows, Mac OSX, ...)

A complete and [**ready to present dynamic HTML report**](http://jmeter.apache.org/usermanual/generating-dashboard.html)

![Dashboard screenshot](https://raw.githubusercontent.com/apache/jmeter/master/xdocs/images/screenshots/dashboard/response_time_percentiles_over_time.png)

[**Live reporting**](http://jmeter.apache.org/usermanual/realtime-results.html) into 3rd party databases like InfluxDB or Graphite

![Live report](https://raw.githubusercontent.com/apache/jmeter/master/xdocs/images/screenshots/grafana_dashboard.png)

Easy correlation through ability to extract data from most popular response formats, [**HTML**](http://jmeter.apache.org/usermanual/component_reference.html#CSS/JQuery_Extractor), [**JSON**](http://jmeter.apache.org/usermanual/component_reference.html#JSON_Extractor), [**XML**](http://jmeter.apache.org/usermanual/component_reference.html#XPath_Extractor) or [**any textual format**](http://jmeter.apache.org/usermanual/component_reference.html#Regular_Expression_Extractor)

Complete portability and 100% Java purity

Full multi-threading framework allows concurrent sampling by many threads
and simultaneous sampling of different functions by separate thread groups.

Caching and offline analysis/replaying of test results.

Highly Extensible core:

- Pluggable Samplers allow unlimited testing capabilities.
- **Scriptable Samplers** (JSR223-compatible languages like Groovy)
- Several load statistics may be chosen with **pluggable tiers**.
- Data analysis and **visualization plugins** allow great exensibility and personalization.
- Functions can be used to provide dynamic input to a test orprovide data manipulation.
- Easy Continuous Integration through 3rd party Open Source libraries for Maven, Gradle and Jenkins

## Requirements

The following requirements exist for running Apache JMeter:

- Java Interpreter:

  A fully compliant Java 8 Runtime Environment is required
  for Apache JMeter to execute. A JDK with `keytool` utility is better suited
  for Recording HTTPS websites.

- Optional jars:

  Some jars are not included with JMeter.
  These should be downloaded and placed in the lib directory
  - Neo4j Java Bolt Driver - https://search.maven.org/artifact/org.neo4j.driver/neo4j-java-driver/1.7.5/jar
  - Google GSON - https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.2/gson-2.8.2.jar

- Java Compiler (*OPTIONAL*):

  A Java compiler is not needed since the distribution cludes a
  precompiled Java binary archive.
  > **Note** that a compiler is required to build plugins for Apache JMeter.

## Installation Instructions

> **Note** that spaces in directory names can cause problems.

### Global GraphHack Build

- Clone https://github.com/graphaware/jmeter
- From the root directory, run `./gradlew build`
- Copy the Bolt protocol plugin with `cp src/protocol/bolt/build/libs/ApacheJMeter_bolt.jar lib/ext/`
- Ensure that the Neo4j Java Bold driver and GSON jars(see above) are placed into the `lib` directory

## Running JMeter

1. Change to the `bin` directory
2. Run the `jmeter` (Un\*x) or `jmeter.bat` (Windows) file.

### Windows

For Windows there are also some other scripts which you can drag-and-drop
a JMX file onto:

- `jmeter-n.cmd` - runs the file as a non-GUI test
- `jmeter-n-r.cmd` - runs the file as a non-GUI remote (client-server) test
- `jmeter-t.cmd` - loads the file ready to run it as a GUI test

## Bolt Plugin for JMeter

### Features

- `Bolt Connection Configuration` Config Element supporting the bolt scheme and basic authentication (username, password)
- `Bolt Request` Sampler supporting parameterised Cypher queries
- Cypher parameter substitution via User Defined Variables and CSV DataSet Config

### Roadmap
- Acceptance into the jMeter distribution
- Support for `bolt+routing` scheme
- Support for other authentication mechanisms such as kerberos
- Connection Pool configuration


### How To
- To your test plan, add a `Bolt Connection Configuration` Config Element and specify the Bolt URI, username and password
- To your thread group(s), add the `Bolt Request` Sampler. 

  - Specify a Cypher statement, parameterised if required.
  - Parameters are specified as a JSON string such as `{"name":"Luanne","country":"UAE"}`. 
  - Variable substitution is also supported in the form
  - By default, the results of queries will not be captured and logged (for example, in the View Results Tree response). It can be enabled by setting `Record Query Results` to `true` but be aware that this will iterate through the entire resultset.

- Add listeners as required. For example, the `View Results Tree` lists each sample along with the request (Cypher query and parameters), and response from Neo4j
- A sample plan can be downloaded from https://github.com/graphaware/neo4j-jmeter-load-tests. This repository also contains the test plan and data used in the demo recording at https://youtu.be/Zqyeo0iShF4
