package tc.oc.pgm.map;

import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.xml.InvalidXMLException;

/**
 * Something that needs to be invoked once for each map parsed.
 *
 * Bind this into {@link MapBinders#rootParsers()} and it will be provisioned
 * and have it's {@link #parse()} method called once at parse time for each map.
 *
 * Keep in mind that if this is {@link MapScoped} then it will be stored permanently
 * with the map. If the object is not needed after parsing then leave it unscoped,
 * and it will be discarded. If it has no map-specific state or dependencies, it
 * can be a Singleton.
 */
public interface MapRootParser {
    void parse() throws InvalidXMLException;
}
