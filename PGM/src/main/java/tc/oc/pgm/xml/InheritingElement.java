package tc.oc.pgm.xml;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Inherits attributes from its parent {@link Element}, if there is one.
 */
public class InheritingElement extends ClonedElement {

    public static InheritingElement of(Element el) {
        return el instanceof InheritingElement ? (InheritingElement) el
                                               : new InheritingElement(el);
    }

    private InheritingElement(Element el) {
        super(el);

        if(getParent() instanceof Element) {
            for(Attribute attribute : ((Element) el.getParent()).getAttributes()) {
                if(getAttribute(attribute.getName()) == null) {
                    setAttribute(attribute.clone());
                }
            }
        }
    }
}
