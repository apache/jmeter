![Apache JMeter logo](https://jmeter.apache.org/images/logo.svg)
# Apache JMeter

## What is it?

Apache JMeter is a 100% pure Java application designed to test
and measure performance.  It may be used as a highly portable 
server benchmark as well as multiclient load generator.

Apache JMeter features include:

Ability to load and performance test many different server/protocol types:
 -  Web - HTTP, HTTPS
 -  SOAP / REST
 -  FTP
 -  Database via JDBC
 -  LDAP
 -  Message-oriented Middleware (MOM) via JMS
 -  Mail - SMTP(S), POP3(S) and IMAP(S)
 -  Native commands or shell scripts
 -  TCP

Full multithreading framework allows concurrent sampling by many threads
and simultaneous sampling of different functions by separate thread groups.
Careful GUI design allows faster Test Plan building and debugging.
Caching and offline analysis/replaying of test results.

Highly Extensible core:
 -  Pluggable Samplers allow unlimited testing capabilities.
 -  Several load statistics may be chosen with pluggable timers .
 -  Data analysis and visualization plugins allow great extensibility as well as personalization.
 -  Functions can be used to provide dynamic input to a test or provide data manipulation.
 -  Scriptable Samplers (BeanShell, BSF- and JSR223- compatible languages)


## The Latest Version

Details of the latest version can be found on the Java Apache 
Project web site (http://jmeter.apache.org/).

## Requirements

The following requirements exist for running Apache JMeter:

 -  Java Interpreter:

    A fully compliant Java 7 (or later) Runtime Environment is required 
    for Apache JMeter to execute.

 -  Optional jars:

    Some jars are not included with JMeter.
    If required, these should be downloaded and placed in the lib directory

    + JDBC - available from database supplier
    + JMS - available from the JMS provider
    + Bouncy Castle - available from http://www.bouncycastle.org/latest_releases.html

      (currently only needed for SMIME Assertion)

 -  Java Compiler [OPTIONAL]:

    A Java compiler is not needed since the distribution includes a
    precompiled Java binary archive. _Note that a compiler is required
    if you plan to build plugins for Apache JMeter._

## Installation Instructions

_Note that spaces in directory names can cause problems._

 - Release builds

   Unpack the binary archive into a suitable directory structure.

## Running JMeter

Change to the bin directory
Run the jmeter (Un\*x) or jmeter.bat (Windows) file.

For Windows there are also some other scripts.
Drag-drop a JMX file onto one of them:

`jmeter-n.cmd` - runs the file as a non-GUI test

`jmeter-n-r.cmd` - runs the file as a non-GUI remote (client-server) test

`jmeter-t.cmd` - loads the file ready to run it as a GUI test

## Documentation

The documentation available as of the date of this release is
also included, in HTML format, in the `printable_docs/` directory,
and it may be browsed starting from the file called `index.html`.

## Build instructions

 - Release builds

   Unpack the source archive into a suitable directory structure.
   Most of the 3rd party library files can be extracted from the binary archive
   by unpacking it into the same directory structure.
   You can also use Ant to download the required library files:

```sh
ant download_jars
```

Any optional jars (see above) should be placed in `lib/opt` and/or `lib`.

Jars in `lib/opt` will be used for building JMeter and running the unit test,
but won't be used at run-time.

_This is useful for testing what happens if the optional jars are not
downloaded by other JMeter users._

JMeter is built using Ant.

Change to the top-level directory and issue the command:

```sh
ant download_jars
```
_Only needs to be done once; will download any missing 3rd party jars_

```sh
ant
```

This will compile the application and enable you to run jmeter from the `bin`
directory.

```sh
ant test [-Djava.awt.headless=true]
```

This will compile and run the unit tests.
The optional property definition is required if the system
does not have a suitable GUI display.

## Developer information

The code is maintained in SVN at https://svn.apache.org/repos/asf/jmeter/trunk

There is a read-only mirror at GitHub: https://github.com/apache/jmeter

## Licensing and legal issues

For legal and licensing issues, please look the files:

[LICENSE](LICENSE)

[NOTICE](NOTICE)

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

  Apache JMeter interfaces (via Apache HttpClient3) with the
  Java Cryptography Extension (JCE) API to provide

    - NTLM authentication

  Apache JMeter does not include any implementation of JSSE or JCE.


**Thank you for using Apache JMeter.**
