package tc.oc.pgm.xml;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Deep-copies a given {@link Element}.
 *
 * Unlike a {@link #clone}d element, calling {@link #getParent} or {@link #getDocument} on this element will
 * return the same value as the original element, even though this element is not actually a child of the
 * original element's parent. This is needed so that copied elements know which document they came from, and
 * can generate proper error messages.
 */
public class ClonedElement extends BoundedElement {

    protected final int indexInParent;

    public ClonedElement(Element el) {
        super(el.getName(), el.getNamespace());
        setParent(el.getParent());

        final BoundedElement bounded = (BoundedElement) el;
        setLine(bounded.getLine());
        setColumn(bounded.getColumn());
        setStartLine(bounded.getStartLine());
        setEndLine(bounded.getEndLine());
        this.indexInParent = bounded.indexInParent();

        setContent(el.cloneContent());

        for(Attribute attribute : el.getAttributes()) {
            setAttribute(attribute.clone());
        }
    }

    @Override
    public int indexInParent() {
        return indexInParent;
    }
}
