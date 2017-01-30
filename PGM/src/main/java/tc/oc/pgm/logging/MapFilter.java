package tc.oc.pgm.logging;

import tc.oc.commons.core.logging.Logging;
import tc.oc.pgm.map.*;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * Include or exclude log {@link LogRecord}s with a {@link PGMMap} object
 * as a parameter. These are created by {@link tc.oc.pgm.map.MapLogger} for XML errors.
 */
public class MapFilter implements Filter {

    private final boolean yes;

    public MapFilter(boolean yes) {
        this.yes = yes;
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        PGMMap map = Logging.getParam(record, PGMMap.class);
        return (yes && map != null) || (!yes && map == null);
    }
}
