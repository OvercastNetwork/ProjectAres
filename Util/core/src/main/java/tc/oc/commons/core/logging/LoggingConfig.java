package tc.oc.commons.core.logging;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingConfig {

    private final Logger logger;

    public LoggingConfig() {
        this.logger = Logger.getLogger("");
        load();
    }

    public void load() {
        try {
            Logger.getLogger("").setLevel(Level.INFO);

            final Properties properties = new Properties();
            try { properties.load(new FileInputStream("logging.properties")); }
            catch(FileNotFoundException ignored) {}

            for(Map.Entry<Object, Object> entry : properties.entrySet()) {
                String[] parts = entry.getKey().toString().split("\\.");
                if(parts.length == 2 && "level".equals(parts[1])) {
                    String loggerName = parts[0];
                    String levelName = (String) entry.getValue();
                    if("root".equals(loggerName)) {
                        loggerName = "";
                    }
                    Logging.setDefaultLevel(loggerName, Level.parse(levelName));
                }
            }

            Logging.updateFromLoggingProperties();
        } catch(IOException | NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.log(Level.WARNING, "Error applying logging config", e);
        }
    }
}
