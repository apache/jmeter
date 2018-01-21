<h1 align="center"><img src="https://jmeter.apache.org/images/logo.svg" alt="Apache JMeter logo" /></h1>
<h4 align="center">Open Source application designed to load test applications and measure performance. By The Apache Software Foundation</h4>
<br>

[![Build Status](https://api.travis-ci.org/apache/jmeter.svg?branch=trunk)](https://travis-ci.org/apache/jmeter/)
[![codecov](https://codecov.io/gh/apache/jmeter/branch/trunk/graph/badge.svg)](https://codecov.io/gh/apache/jmeter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.jmeter/ApacheJMeter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.jmeter/ApacheJMeter)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Stack Overflow](https://img.shields.io/:stack%20overflow-jmeter-brightgreen.svg)](https://stackoverflow.com/questions/tagged/jmeter)
[![Twitter](https://img.shields.io/twitter/url/https/github.com/apache/jmeter.svg?style=social)](https://twitter.com/intent/tweet?text=Powerful%20load%20testing%20with%20Apache%20JMeter:&url=https://jmeter.apache.org)

## What is it?

Apache JMeter may be used to test performance both on static and dynamic resources, Web dynamic applications.
It can be used to simulate a heavy load on a server, group of servers, network or object to test its strength or to analyze overall performance under different load types.

![Image of JMeter 4.0](https://raw.githubusercontent.com/apache/jmeter/trunk/xdocs/images/screenshots/JMETER_4.0.png)

Apache JMeter features include:

Ability to load and performance test many different applications/server/protocol types: 
 -  Web - HTTP, HTTPS (Java, NodeJS, PHP, ASP.NET,...)
 -  SOAP / REST Webservices
 -  FTP
 -  Database via JDBC
 -  LDAP
 -  Message-oriented Middleware (MOM) via JMS
 -  Mail - SMTP(S), POP3(S) and IMAP(S)
 -  Native commands or shell scripts
 -  TCP
 -  Java Objects

Full featured Test IDE that allows fast Test Plan **recording (from Browsers or native applications), building and debugging.**

[**Command-line mode (Non GUI / headless mode)**](http://jmeter.apache.org/usermanual/get-started.html#non_gui) to load test from any Java compatible OS (Linux, Windows, Mac OSX, ...)

A complete and [**ready to present dynamic HTML report**](http://jmeter.apache.org/usermanual/generating-dashboard.html)

![Dashboard screenshot](https://raw.githubusercontent.com/apache/jmeter/trunk/xdocs/images/screenshots/dashboard/response_time_percentiles_over_time.png)

[**Live reporting**](http://jmeter.apache.org/usermanual/realtime-results.html) into 3rd party databases like InfluxDB or Graphite

![Live report](https://raw.githubusercontent.com/apache/jmeter/trunk/xdocs/images/screenshots/grafana_dashboard.png)

Easy correlation through ability to extract data from most popular response formats, [**HTML**](http://jmeter.apache.org/usermanual/component_reference.html#CSS/JQuery_Extractor), [**JSON**](http://jmeter.apache.org/usermanual/component_reference.html#JSON_Extractor), [**XML**](http://jmeter.apache.org/usermanual/component_reference.html#XPath_Extractor) or [**any textual format**](http://jmeter.apache.org/usermanual/component_reference.html#Regular_Expression_Extractor)

Complete portability and 100% Java purity

Full multi-threading framework allows concurrent sampling by many threads
and simultaneous sampling of different functions by separate thread groups.

Caching and offline analysis/replaying of test results.

Highly Extensible core:
 -  Pluggable Samplers allow unlimited testing capabilities.
 -  **Scriptable Samplers** (JSR223-compatible languages like Groovy)
 -  Several load statistics may be chosen with **pluggable timers**.
 -  Data analysis and **visualization plugins** allow great extensibility and personalization.
 -  Functions can be used to provide dynamic input to a test or provide data manipulation.
 -  Easy Continuous Integration through 3rd party Open Source libraries for Maven, Graddle and Jenkins


## The Latest Version

Details of the latest version can be found on the JMeter Apache 
Project web site (https://jmeter.apache.org/).

## Requirements

The following requirements exist for running Apache JMeter:

*  Java Interpreter:

    A fully compliant Java 8 Runtime Environment is required 
    for Apache JMeter to execute. A JDK with keytool utility is better suited 
    for Recording HTTPS websites. 
    Java 9 is not yet supported as of version 3.3.

*  Optional jars:

    Some jars are not included with JMeter.
    If required, these should be downloaded and placed in the lib directory

    * JDBC - available from the database supplier
    * JMS - available from the JMS provider
    * [Bouncy Castle](http://www.bouncycastle.org/latest_releases.html) - 
    only needed for SMIME Assertion

*  Java Compiler (OPTIONAL):

    A Java compiler is not needed since the distribution includes a
    precompiled Java binary archive. _Note that a compiler is required
    to build plugins for Apache JMeter._

## Installation Instructions

_Note that spaces in directory names can cause problems._

 * Release builds

   Unpack the binary archive into a suitable directory structure.

## Running JMeter

1. Change to the `bin` directory
2. Run the `jmeter` (Un\*x) or `jmeter.bat` (Windows) file.

### Windows

For Windows there are also some other scripts which you can drag-and-drop
a JMX file onto:

* `jmeter-n.cmd` - runs the file as a non-GUI test
* `jmeter-n-r.cmd` - runs the file as a non-GUI remote (client-server) test
* `jmeter-t.cmd` - loads the file ready to run it as a GUI test

## Documentation

The documentation available as of the date of this release is
also included, in HTML format, in the `printable_docs/` directory,
and it may be browsed starting from the file called `index.html`.

## Reporting a bug/enhancement

See [Issue Tracking](https://jmeter.apache.org/issues.html)

## Build instructions

### Release builds

Unpack the source archive into a suitable directory structure.
Most of the 3rd party library files can be extracted from the binary archive
by unpacking it into the same directory structure.
You can also use Ant to download the required library files:

```sh
ant download_jars
```

Any optional jars (see above) should be placed in `lib/opt` and/or `lib`.

Jars in `lib/opt` will be used for building JMeter and running the unit tests,
but won't be used at run-time.

_This is useful for testing what happens if the optional jars are not
downloaded by other JMeter users._

If you are behind a proxy, you can set a few build properties in `build-local.properties` for ant to use the proxy:

```
proxy.use=true
proxy.host=proxy.example.invalid
proxy.port=8080
proxy.user=your_user_name
proxy.pass=your_password
```

You might also want to skip some tests - that are failing without proper access to the internet - by adding some more
properties into `build-local.properties`:
```
skip.bug52310=true
skip.bug60607=true
skip.batchtest_Http4ImplPreemptiveBasicAuth=true
skip.batchtest_SlowCharsFeature=true
skip.batchtest_TestKeepAlive=true
skip.batchtest_ResponseDecompression=true
skip.test_http=true
skip.test_TestDNSCacheManager.testWithCustomResolverAnd1Server=true
```

### Test builds

JMeter is built using Ant.

Change to the top-level directory and issue the command:

```sh
ant download_jars
```
_This only needs to be done once; it will download any missing 3rd party jars._

```sh
ant
```

This will compile the application and enable you to run `jmeter` from the `bin`
directory.

```sh
ant test [-Djava.awt.headless=true]
```

This will compile and run the unit tests.
The optional property definition is required if the system
does not have a suitable GUI display.

## Developer information

Building and contributing is explained in details at https://jmeter.apache.org/building.html

The code is maintained in SVN at https://svn.apache.org/repos/asf/jmeter/trunk

There is a read-only mirror at GitHub: https://github.com/apache/jmeter

## Licensing and legal information

For legal and licensing information, please see the following files:

* [LICENSE](LICENSE)

* [NOTICE](NOTICE)

## Cryptographic Software Notice

This distribution may include software that has been designed for use
with cryptographic software. The country in which you currently reside
may have restrictions on the import, possession, use, and/or re-export
to another country, of encryption software. BEFORE using any encryption
software, please check your country's laws, regulations and policies
concerning the import, possession, or use, and re-export of encryption
software, to see if this is permitted. See <http://www.wassenaar.org/>
for more information.

The U.S. Government Department of Commerce, Bureau of Industry and
Security (BIS), has classified this software as Export Commodity
Control Number (ECCN) 5D002.C.1, which includes information security
software using or performing cryptographic functions with asymmetric
algorithms. The form and manner of this Apache Software Foundation
distribution makes it eligible for export under the License Exception
ENC Technology Software Unrestricted (TSU) exception (see the BIS
Export Administration Regulations, Section 740.13) for both object
code and source code.

The following provides more details on the included software that
may be subject to export controls on cryptographic software:

  Apache JMeter interfaces with the
  Java Secure Socket Extension (JSSE) API to provide

    - HTTPS support

  Apache JMeter interfaces (via Apache HttpClient4) with the
  Java Cryptography Extension (JCE) API to provide

    - NTLM authentication

  Apache JMeter does not include any implementation of JSSE or JCE.


**Thank you for using Apache JMeter.**
