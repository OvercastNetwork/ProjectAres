package tc.oc.pgm.map;

import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.jdom2.Document;
import tc.oc.pgm.xml.InvalidXMLException;

/**
 * An interface used by legacy map parsing code
 */
public interface MapModuleParser<M extends MapModule> {
    /**
     * Create a {@link MapModule} from the given XML document,
     * or return null to indicate that the module is not needed.
     *
     * If a module is returned, it's members are injected automatically,
     * which means this method must NOT inject anything into the module
     * itself.
     */
    @Nullable M parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException;
}
