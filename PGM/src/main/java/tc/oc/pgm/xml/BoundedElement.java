package tc.oc.pgm.xml;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.located.LocatedElement;

/**
 * This exists in order to capture both start and ending lines of elements for error messages.
 * {@link LocatedElement} only captures the ending line, which is rather unintuitive.
 *
 * {@link BoundedJDOMFactory} and {@link BoundedSAXHandler} are mostly just hoop-jumping to make JDOM use this class.
 */
public class BoundedElement extends LocatedElement {

    private int startLine, endLine;
    private int indexInParent = Integer.MIN_VALUE;

    public BoundedElement(String name, Namespace namespace) {
        super(name, namespace);
    }

    public BoundedElement(String name) {
        super(name);
    }

    public BoundedElement(String name, String uri) {
        super(name, uri);
    }

    public BoundedElement(String name, String prefix, String uri) {
        super(name, prefix, uri);
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        setLine(startLine);
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int indexInParent() {
        if(indexInParent < -1) {
            final Element parent = getParentElement();
            indexInParent =  parent == null ? -1 : parent.indexOf(this);
        }
        return indexInParent;
    }

    @Override
    public BoundedElement clone() {
        final BoundedElement that = (BoundedElement) super.clone();
        that.setLine(getLine());
        that.setColumn(getColumn());
        that.setStartLine(getStartLine());
        that.setEndLine(getEndLine());
        return that;
    }
}
