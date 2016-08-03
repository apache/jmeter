package org.apache.jmeter.protocol.http.parser;

import com.helger.css.handler.LoggingCSSParseExceptionCallback;
import com.helger.css.parser.ParseException;
import com.helger.css.reader.errorhandler.LoggingCSSParseErrorHandler;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;

final class CSSParseExceptionCallback extends LoggingCSSParseExceptionCallback {
  private static final Logger LOG = LoggingManager.getLoggerForClass();
  private static final boolean IGNORE_UNRECOVERABLE_PARSING_ERROR = JMeterUtils.getPropDefault("httpsampler.ignore_failed_embedded_resource", false); //$NON-NLS-1$
  private static final long serialVersionUID = -9111232037888068394L;

  private final URL cssUrl;

  CSSParseExceptionCallback(final URL cssUrl) {
    this.cssUrl = checkNotNull(cssUrl);
  }

  @Override
  public void onException(final ParseException ex) {
    if(IGNORE_UNRECOVERABLE_PARSING_ERROR) {
      LOG.warn("Failed to parse CSS: " + cssUrl + ", " + LoggingCSSParseErrorHandler.createLoggingStringParseError (ex));
    } else {
      throw new IllegalStateException("Failed to parse CSS: " + cssUrl + ", " + LoggingCSSParseErrorHandler.createLoggingStringParseError (ex));
    }
  }
}