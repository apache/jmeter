Directories
===========
lib - utility jars
lib/api - Directory where API spec libraries live.
lib/doc - jars needed for generating documentation. Not included with JMeter releases.
lib/ext - JMeter jars only
lib/junit - test jar for JUnit sampler
lib/opt - Directory where Optional 3rd party libraries live
lib/src - storage area for source and javadoc jars, e.g. for use in IDEs
          Excluded from SVN, not included in classpath

Which jars are used by which modules?
====================================
[not exhaustive]

avalon-framework-4.1.4 (org.apache.avalon.framework)
----------------------
- LogKit (LoggingManager)
- Configuration (DataSourceElement)
- OldSaveService

bsf-2.4.0.jar (org.apache.bsf)
-------------
http://jakarta.apache.org/site/downloads/downloads_bsf.cgi
- BSF test elements (sampler etc.)

bsh-2.0b5.jar (org.bsh)
-------------
- BeanShell test elements

commons-codec-1.10
-----------------
http://commons.apache.org/downloads/download_codec.cgi
- used by commons-httpclient-3.1
- also HtmlParserTester for Base64

commons-collections-3.2.1
-------------------------
http://commons.apache.org/downloads/download_collections.cgi
- ListenerNotifier
- Anakia

commons-httpclient-3.1
----------------------
http://hc.apache.org/downloads.cgi
- httpclient version of HTTP sampler
- Cookie manager implementation

commons-io-2.4
--------------
http://commons.apache.org/downloads/download_io.cgi
- FTPSampler

commons-jexl-1.1
----------------
http://commons.apache.org/downloads/download_jexl.cgi
- Jexl function and BSF test elements

commons-lang-2.6
----------------
http://commons.apache.org/downloads/download_lang.cgi
- velocity (Anakia)

commons-lang3-3.3.4
----------------
http://commons.apache.org/downloads/download_lang.cgi
- URLCollection (unescapeXml)

commons-logging-1.2
---------------------
http://commons.apache.org/downloads/download_logging.cgi
- httpclient

commons-math3-3.4.1
-----------------
http://commons.apache.org/proper/commons-math/download_math.cgi
- BackendListener

commons-net-3.3
-----------------
http://commons.apache.org/downloads/download_net.cgi
- FTPSampler

commons-pool2-2.3
-----------------
http://commons.apache.org/proper/commons-pool/download_pool.cgi
- BackendListener

dnsjava-2.1.7
-----------------
http://www.dnsjava.org/download/
- DNSCacheManager

excalibur-datasource-1.1.1 (org.apache.avalon.excalibur.datasource)
--------------------------
- DataSourceElement (JDBC)

excalibur-instrument-1.0 (org.apache.excalibur.instrument)
------------------------
- used by excalibur-datasource

excalibur-logger-1.1 (org.apache.avalon.excalibur.logger)
--------------------
- LoggingManager

excalibur-pool-1.2 (org.apache.avalon.excalibur.pool)
------------------
- used by excalibur-datasource

htmlparser-2.1
htmllexer-2.1
----------------------
http://htmlparser.sourceforge.net/
- http: parsing html

jCharts-0.7.5 (org.jCharts)
-------------
http://jcharts.sourceforge.net/downloads.html
- AxisGraph,LineGraph,LineChart

jdom-1.1.3
--------
http://www.jdom.org/downloads/index.html
- XMLAssertion, JMeterTest ONLY
- Anakia

jodd-core-3.6.4
--------
http://www.jodd.org/
- CSS/JQuery like extractor dependency

jodd-lagarto-3.6.4
--------
http://jodd.org/doc/csselly/
- CSS/JQuery like extractor

jodd-log-3.6.4
--------
http://www.jodd.org/
- CSS/JQuery like extractor dependency

jsoup-1.8.1
--------
http://www.jsoup.org/
- CSS/JQuery like extractor

rhino-1.7R5
--------
http://www.mozilla.org/rhino/download.html
- javascript function
- IfController
- WhileController
- BSF (Javascript)

jTidy-r938
----
- http: various modules for parsing html
- org.xml.sax - various
- XPathUtil (XPath assertion)

junit 4.12
-----------
- unit tests, JUnit sampler

HttpComponents (HttpComponents Core 4.x and HttpComponents Client 4.x)
-----------
http://hc.apache.org/
- httpclient 4 implementation for HTTP sampler 

logkit-2.0
----------
- logging
- Anakia

mongo-java-driver 2.11.3
------------------------
http://www.mongodb.org/
- MongoDB sampler

oro-2.0.8
---------
http://jakarta.apache.org/site/downloads/downloads_oro.cgi
- regular expressions: various

rsyntaxtextarea-2.5.6
---------------------
http://fifesoft.com/rsyntaxtextarea/
- syntax coloration

serialiser-2.7.1
----------------
http://www.apache.org/dyn/closer.cgi/xml/xalan-j
- xalan

slf4j-api-1.7.10, slf4j-nop-1.7.10
----------------
http://www.slf4j.org/
- jodd-core

soap-2.3.1
----------
- WebServiceSampler ONLY

tika-1.7
--------------
http://tika.apache.org/
- Regular Expression Extractor

velocity-1.7
--------------
http://velocity.apache.org/download.cgi
- Anakia (create documentation) Not used by JMeter runtime

xalan_2.7.1
-----------
http://www.apache.org/dyn/closer.cgi/xml/xalan-j
+org.apache.xalan|xml|xpath

xercesimpl-2.11.0
----------------
http://xerces.apache.org/xerces2-j/download.cgi
+org.apache.html.dom|org.apache.wml|org.apache.xerces|org.apache.xml.serialize
+org.w3c.dom.html|ls

xml-apis-1.4.01
--------------
http://xerces.apache.org/xerces2-j/download.cgi
+javax.xml
+org.w3c.dom
+org.xml.sax

The x* jars above are used for XML handling

xmlgraphics-commons-1.5 (org.apache.xmlgraphics.image.codec)
------------------
http://xmlgraphics.apache.org/commons/download.html
- SaveGraphicsService

xmlpull-1.1.3.1
---------------
http://www.xmlpull.org/impls.shtml
- xstream


xpp3_min-1.1.4c
---------------
http://xstream.codehaus.org/download.html
or
http://www.extreme.indiana.edu/dist/java-repository/xpp3/distributions/
- xstream

xstream-1.4.6
-------------
http://xstream.codehaus.org/download.html
- SaveService