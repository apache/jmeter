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

bsf-2.4.0.jar (org.apache.bsf)
-------------
http://jakarta.apache.org/site/downloads/downloads_bsf.cgi
- BSF test elements (sampler etc.)

bsh-2.0b5.jar (org.bsh)
-------------
- BeanShell test elements

commons-codec-1.4
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

commons-io-1.4
--------------
http://commons.apache.org/downloads/download_io.cgi
- FTPSampler

commons-jexl-1.1
----------------
http://commons.apache.org/downloads/download_jexl.cgi
- Jexl function and BSF test elements

commons-lang-2.4
----------------
http://commons.apache.org/downloads/download_lang.cgi
- velocity (Anakia)
- URLCollection (unescapeXml)

commons-logging-1.1.1
---------------------
http://commons.apache.org/downloads/download_logging.cgi
- httpclient

commons-net-1.4.1
-----------------
http://commons.apache.org/downloads/download_net.cgi
- FTPSampler

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

htmlparser-2.0-20060923
htmllexer-2.0-20060923
----------------------
http://htmlparser.sourceforge.net/
- http: parsing html

jCharts-0.7.5 (org.jCharts)
-------------
http://jcharts.sourceforge.net/downloads.html
- AxisGraph,LineGraph,LineChart

jdom-1.1
--------
http://www.jdom.org/downloads/index.html
- XMLAssertion, JMeterTest ONLY
- Anakia

js-1.6R5
--------
http://www.mozilla.org/rhino/download.html
- javascript function
- BSF (Javascript)

jTidy-r938
----
- http: various modules for parsing html
- org.xml.sax - various
- XPathUtil (XPath assertion)

junit 4.8.1
-----------
- unit tests, JUnit sampler

logkit-2.0
----------
- logging
- Anakia

oro-2.0.8
---------
http://jakarta.apache.org/site/downloads/downloads_oro.cgi
- regular expressions: various

serialiser-2.7.1
----------------
http://www.apache.org/dyn/closer.cgi/xml/xalan-j
- xalan

soap-2.3.1
----------
- WebServiceSampler ONLY

velocity-1.6.2
--------------
http://velocity.apache.org/download.cgi
- Anakia (create documentation) Not used by JMeter runtime

xalan_2.7.1
-----------
http://www.apache.org/dyn/closer.cgi/xml/xalan-j
+org.apache.xalan|xml|xpath

xercesimpl-2.9.1
----------------
http://xerces.apache.org/xerces2-j/download.cgi
+org.apache.html.dom|org.apache.wml|org.apache.xerces|org.apache.xml.serialize
+org.w3c.dom.html|ls

xml-apis-1.3.04
--------------
http://xerces.apache.org/xerces2-j/download.cgi
+javax.xml
+org.w3c.dom
+org.xml.sax

The x* jars above are used for XML handling

xmlgraphics-commons-1.3.1 (org.apache.xmlgraphics.image.codec)
------------------
http://xmlgraphics.apache.org/commons/download.html
- SaveGraphicsService

xpp3_min-1.1.4c
---------------
http://xstream.codehaus.org/download.html
or
http://www.extreme.indiana.edu/dist/java-repository/xpp3/distributions/
- xstream

xstream-1.3.1
-------------
http://xstream.codehaus.org/download.html
- SaveService