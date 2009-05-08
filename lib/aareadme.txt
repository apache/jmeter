Which jars are used by which modules?
====================================
[not exhaustive]

avalon-framework-4.1.4 (org.apache.avalon.framework)
----------------------
- LogKit (used by HttpClient ?)
- Configuration (ResultCollector, SaveService, SampleResult, TestElementSaver)

batik-codec-1.7 (org.apache.batik.ext.image.codec)
------------------
Download: http://xmlgraphics.apache.org/batik/download.cgi
- SaveGraphicsService

commons-codec-1.3
-----------------
http://commons.apache.org/downloads/download_codec.cgi
- used by commons-httpclient-3.1
- also HtmlParserTester for Base64

commons-collections-3.2
-----------------------
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
- jdbc - DataSourceElement
- JDBCSampler

excalibur-instrument-1.0 (org.apache.excalibur.instrument)
------------------------
- used by excalibur-datasource

excalibur-logger-1.1 (org.apache.avalon.excalibur.logger)
--------------------
- httpclient?
- LoggingManager

excalibur-pool-1.2 (org.apache.avalon.excalibur.pool)
------------------
- used by excalibur-datasource

htmlparser-2.0-20060923
htmllexer-2.0-20060923
----------------------
http://htmlparser.sourceforge.net/
- http: parsing html

jakarta-oro-2.0.8
-----------------
http://jakarta.apache.org/site/downloads/downloads_oro.cgi
- regular expressions: various

jCharts-0.7.5 (org.jCharts)
-------------
http://jcharts.sourceforge.net/downloads.html
- AxisGraph,LineGraph,LineChart

jdom-1.1
--------
http://www.jdom.org/downloads/index.html
- XMLAssertion, JMeterTest ONLY
- Anakia

js_rhino1_6R5
-------------
http://www.mozilla.org/rhino/download.html
- javascript function

junit 3.8.2
-----------
- unit tests

logkit-1.2
----------
- logging
- Anakia

serialiser-2_9_1
----------------
http://www.apache.org/dyn/closer.cgi/xml/xalan-j
- xalan

soap (appears to be version 2.3.1)
----
- WebServiceSampler ONLY

Tidy
----
- http: various modules for parsing html
- org.xml.sax - various
- XPathUtil (XPath assertion)

velocity-1.6.2
--------------
http://velocity.apache.org/download.cgi
- Anakia (create documentation) Not used by JMeter runtime

xalan_2_7_1
-----------
http://www.apache.org/dyn/closer.cgi/xml/xalan-j
+org.apache.xalan|xml|xpath

xercesimpl-2_9_1
----------------
http://xerces.apache.org/xerces2-j/download.cgi
+org.apache.html.dom|org.apache.wml|org.apache.xerces|org.apache.xml.serialize
+org.w3c.dom.html|ls

xml-apis-2_9_1
--------------
http://xerces.apache.org/xerces2-j/download.cgi
+javax.xml
+org.w3c.dom
+org.xml.sax

The x* jars above are used for XML handling

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