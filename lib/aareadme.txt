Which jars are used by which modules?
====================================
[not exhaustive]

avalon-framework-4.1.4 (org.apache.avalon.framework)
- LogKit (used by HttpClient ?)
- Configuration (ResultCollector, SaveService, SampleResult, TestElementSaver)

batik-awt-util-1.6 (org.apache.batik.ext)
- SaveGraphicsService

commons-codec-1.3
- used by commons-httpclient-3.0
- also HtmlParserTester for Base64

commons-collections-3.2
- ListenerNotifier
- Anakia

commons-httpclient-3.1-rc1
- httpclient

commons-io-1.3.1
- FTPSampler

commons-jexl-1.1
- Jexl function

commons-lang-2.3
- velocity (Anakia)
- URLCollection (unescapeXml)

commons-logging-1.1
- httpclient

commons-net-1.4.1
- FTPSampler

excalibur-datasource-1.1.1 (org.apache.avalon.excalibur.datasource)
- jdbc - DataSourceElement
- JDBCSampler

excalibur-instrument-1.0 (org.apache.excalibur.instrument)
- used by excalibur-datasource

excalibur-logger-1.1 (org.apache.avalon.excalibur.logger)
- httpclient?
- LoggingManager

excalibur-pool-1.2 (org.apache.avalon.excalibur.pool)
- used by excalibur-datasource

htmlparser-2.0-20060923
htmllexer-2.0-20060923
- http: parsing html

jakarta-oro-2.0.8
- regular expressions: various

jCharts-0.7.5 (org.jCharts)
- AxisGraph,LineGraph,LineChart

jdom-1.0
- XMLAssertion, JMeterTest ONLY

jdom-b9
- Anakia

(jorphan)

js_rhino1_6R5
- javascript function

junit3.8.2
- unit tests

logkit-1.2
- logging
- Anakia

soap (appears to be version 2.3.1)
- WebServiceSampler ONLY

Tidy
- http: various modules for parsing html
- org.xml.sax - various
- XPathUtil (XPath assertion)

velocity-1.5
- Anakia (create documentation) Not used by JMeter runtime

xalan
+org.apache.xalan|xml|xpath

xercesimpl
+org.apache.html.dom|org.apache.wml|org.apache.xerces|org.apache.xml.serialize
+org.w3c.dom.html|ls

xml-apis
+javax.xml
+org.w3c.dom
+org.xml.sax

The x* jars above are used for XML handling (probably not needed for JDK1.4)

xpp3_min-1.1.3.4.O
- xstream

xstream-1.2.1
- SaveService