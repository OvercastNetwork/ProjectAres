package tc.oc.commons.core.inject;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.spi.Message;

/**
 * Dumps all visited elements to a logger, along with the source location.
 */
public class ElementLogger extends ElementInspector<Void> {

    private final Logger logger;
    private final Level level;

    public ElementLogger(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public Void visit(Message message) {
        logger.log(level, message.getMessage() + " (at " + message.getSource() + ")");
        return null;
    }
}
