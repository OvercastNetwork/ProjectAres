package tc.oc.pgm.xml.property;

import org.jdom2.Element;

public interface PropertyBuilderFactory<T, B extends PropertyBuilder<T, B>> {
    B property(Element parent, String name);
}
