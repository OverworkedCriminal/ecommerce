package ecommerce.configuration.logs;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

/**
 * Class allows to set custom color scheme for highlighting log level
 */
public class LogsHighlighter extends ForegroundCompositeConverterBase<ILoggingEvent>{

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        final var level = event.getLevel();

        return switch (level.levelInt) {
            case Level.TRACE_INT -> ANSIConstants.MAGENTA_FG;
            case Level.DEBUG_INT -> ANSIConstants.BLUE_FG;
            case Level.INFO_INT  -> ANSIConstants.GREEN_FG;
            case Level.WARN_INT  -> ANSIConstants.YELLOW_FG;
            case Level.ERROR_INT -> ANSIConstants.RED_FG;
            default -> ANSIConstants.DEFAULT_FG;
        };
    }
    
}
