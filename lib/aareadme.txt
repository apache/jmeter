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

asm-7.0 (org.ow2.asm)
----------------------
- JSON Path extractor

accessors-smart-1.2 (net.minidev)
----------------------
- JSON Path extractor

bsf-2.4.0.jar (org.apache.bsf)
-------------
http://jakarta.apache.org/site/downloads/downloads_bsf.cgi
- BSF test elements (sampler etc.)

bsh-2.0b6.jar (org.bsh)
-------------
- BeanShell test elements

dec-0.1.2 (org.brotli.dec)
-----------------
https://github.com/google/brotli/tree/master/java/org/brotli
- Used by HTTP4 HC Impl for brotli decoding

caffeine 2.6.2
-----------------
https://github.com/ben-manes/caffeine/
- Used by CssParser
- Used by JMS Publisher Sampler

commons-codec-1.11
-----------------
http://commons.apache.org/downloads/download_codec.cgi
- Used by ProxyControl and JSR223Test Element for Base64 and md5 

commons-collections-3.2.2
-------------------------
http://commons.apache.org/downloads/download_collections.cgi
- ListenerNotifier
- Anakia

commons-io-2.6
--------------
http://commons.apache.org/downloads/download_io.cgi
- FTPSampler

commons-jexl-2.1.1, commons-jexl3-3.1
----------------
http://commons.apache.org/downloads/download_jexl.cgi
- Jexl function and BSF test elements

commons-lang-2.6
----------------
http://commons.apache.org/downloads/download_lang.cgi
- velocity (Anakia)

commons-lang3-3.8.1
----------------
http://commons.apache.org/downloads/download_lang.cgi
- URLCollection (unescapeXml)

commons-math3-3.6.1
-----------------
http://commons.apache.org/proper/commons-math/download_math.cgi
- BackendListener

commons-net-3.6
-----------------
http://commons.apache.org/downloads/download_net.cgi
- FTPSampler

commons-pool2-2.6.0
-----------------
http://commons.apache.org/proper/commons-pool/download_pool.cgi
- BackendListener

commons-text-1.1
-----------------
https://commons.apache.org/proper/commons-text/
- Random Strings

darcula
-----------------
https://github.com/bulenkov/Darcula/
- Look and Feel

dnsjava-2.1.8
-----------------
http://www.dnsjava.org/download/
- DNSCacheManager

groovy-all-2.4.16
----------------------
Advised scripting language for JSR223 Test Elements

hamcrest-core-1.3
----------------------
- unit tests, JUnit sampler
https://github.com/hamcrest/JavaHamcrest

freemarker-2.3.28.jar
----------------------
- used by Report/Dashboard feature

javax.activation-api-1.2.0.jar
----------------------
- used by SMTP Sampler

javax.activation-1.2.0.jar
----------------------
- used by SMTP Sampler

jackson-annotations-2.9.8 (com.fasterxml.jackson)
----------------------

Used by JsonExporter in report generator (com.fasterxml.jackson)
----------------------
jackson-annotations-2.9.8 (https://github.com/FasterXML/jackson-annotations)
jackson-core-2.9.8 (https://github.com/FasterXML/jackson-core)
jackson-databind-2.9.8 (https://github.com/FasterXML/jackson-databind)

jCharts-0.7.5 (org.jCharts)
-------------
http://jcharts.sourceforge.net/downloads.html
- AxisGraph,LineGraph,LineChart

jdom-1.1.3
--------
http://www.jdom.org/downloads/index.html
- Anakia

jodd-core-5.0.6
--------
http://www.jodd.org/
- CSS/JQuery like extractor dependency

jodd-lagarto-5.0.6
--------
http://jodd.org/doc/csselly/
- CSS/JQuery like extractor

jodd-log-5.0.6
--------
http://www.jodd.org/
- CSS/JQuery like extractor dependency

jodd-props-5.0.6
--------
http://www.jodd.org/
- used by Report/Dashboard feature properties management

json-path-2.4.0
--------
https://github.com/jayway/JsonPath
- JSON Path Extractor
- JSON Path Renderer

json-smart-2.3 (net.minidev)
--------
https://github.com/netplex/json-smart-v2
- JSON Path Extractor
- JSON Path Renderer

jsoup-1.11.3
--------
http://www.jsoup.org/
- CSS/JQuery like extractor

log4j2-2.11.1
--------
https://logging.apache.org/log4j/2.x/
- Logging framework

ph-css-6.1.1
--------
https://github.com/phax/ph-css
- CssParser

ph-commons-9.2.1
--------
https://github.com/phax/ph-commons
- CssParser

rhino-1.7.10
--------
https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino/Download_Rhino
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

HttpComponents
- HttpComponents Core 4.4.10
- HttpComponents Client 4.5.7
- HttpComponents AsyncClient 4.1.4
- HttpComponents Core NIO 4.4.11
-----------
http://hc.apache.org/
- httpclient 4 implementation for HTTP sampler
- httpasyncclient implementation for InfluxDB Backend Listener Client

mongo-java-driver 2.11.3
------------------------
http://www.mongodb.org/
- MongoDB sampler

oro-2.0.8
---------
http://jakarta.apache.org/site/downloads/downloads_oro.cgi
- regular expressions: various

rsyntaxtextarea-3.0.2
---------------------
http://fifesoft.com/rsyntaxtextarea/
- syntax coloration

serialiser-2.7.1
----------------
http://www.apache.org/dyn/closer.cgi/xml/xalan-j
- xalan

slf4j-api-1.7.25
----------------
http://www.slf4j.org/
- jodd-core
- json-path
- jmeter internal logging

tika-1.20
--------------
http://tika.apache.org/
- Regular Expression Extractor

commons-dbcp2-2.5.0 (org.apache.commons.dbcp2)
--------------------------
- DataSourceElement (JDBC)

Saxon-HE-9.9.1-1 (net.sf.saxon)
--------------------------
- XPath2Extractor (XML)

velocity-1.7
--------------
http://velocity.apache.org/download.cgi
- Anakia (create documentation) Not used by JMeter runtime

xalan_2.7.1
-----------
http://www.apache.org/dyn/closer.cgi/xml/xalan-j
+org.apache.xalan|xml|xpath

xercesImpl-2.12.0
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

xmlgraphics-commons-2.3 (org.apache.xmlgraphics.image.codec)
------------------
http://xmlgraphics.apache.org/commons/download.html
- SaveGraphicsService

xmlpull-1.1.3.1
---------------
http://www.xmlpull.org/impls.shtml
- xstream


xpp3_min-1.1.4c
---------------
http://x-stream.github.io/download.html
or
http://www.extreme.indiana.edu/dist/java-repository/xpp3/distributions/
- xstream

xstream-1.4.11
-------------
http://x-stream.github.io/download.html
- SaveService
