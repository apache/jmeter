package org.apache.jmeter.protocol.http.parser;

import com.google.common.cache.CacheLoader;
import com.helger.css.ECSSVersion;
import com.helger.css.decl.CSSImportRule;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.decl.visit.CSSVisitor;
import com.helger.css.decl.visit.DefaultCSSUrlVisitor;
import com.helger.css.reader.CSSReader;
import com.helger.css.reader.CSSReaderSettings;
import com.helger.css.reader.errorhandler.LoggingCSSParseErrorHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

final class CssParserCacheLoader extends CacheLoader<Triple<String, URL, Charset>, URLCollection> {
  private static final Logger LOG = LoggingManager.getLoggerForClass();

  @Override
  public URLCollection load(final Triple<String, URL, Charset> triple) throws Exception {
    final String cssContent = triple.getLeft();
    final URL baseUrl = triple.getMiddle();
    final Charset charset = triple.getRight();

    final CascadingStyleSheet aCSS = CSSReader.readFromStringStream(cssContent,
            new CSSReaderSettings()
                    .setBrowserCompliantMode(true)
                    .setFallbackCharset(charset)
                    .setCSSVersion(ECSSVersion.CSS30)
                    .setCustomErrorHandler(new LoggingCSSParseErrorHandler())
                    .setCustomExceptionHandler(new CSSParseExceptionCallback(baseUrl)));
    final URLCollection urls = new URLCollection(new ArrayList<>());

    if (aCSS == null) {
      LOG.warn("Failed parsing url:" + baseUrl + ", got null CascadingStyleSheet");
      return urls;
    }

    CSSVisitor.visitCSSUrl(aCSS, new DefaultCSSUrlVisitor() {
      @Override
      public void onImport(final CSSImportRule importRule) {
        final String location = importRule.getLocationString();
        if (!StringUtils.isEmpty(location)) {
          urls.addURL(location, baseUrl);
        }
      }
    });

    return urls;
  }

}