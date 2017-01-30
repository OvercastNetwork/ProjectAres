package tc.oc.pgm.xml.parser;

import tc.oc.pgm.xml.Parseable;

/**
 * Base interface for generated reflective parsers
 */
public interface ReflectiveParser<T extends Parseable> extends ElementParser<T> {}
