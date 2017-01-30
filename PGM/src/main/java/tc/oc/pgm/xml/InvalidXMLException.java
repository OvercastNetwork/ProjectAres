package tc.oc.pgm.xml;

import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.UncheckedExecutionException;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.JDOMParseException;
import tc.oc.commons.core.util.ThrowingSupplier;
import tc.oc.pgm.module.ModuleLoadException;

public class InvalidXMLException extends ModuleLoadException {
    private static final long serialVersionUID = 5732248895863659936L;

    private @Nullable Node node;
    private @Nullable Document document;
    private @Nullable String documentPath;
    private int startLine, endLine, column;

    protected InvalidXMLException(String message, @Nullable Node node, @Nullable Document document,
                                  @Nullable String documentPath, int startLine, int endLine, int column,
                                  Throwable cause) {
        super(message, cause);
        this.node = node;
        this.document = document != null ? document : node != null ? node.getDocument() : null;

        this.documentPath = documentPath != null ? documentPath : this.document != null ? this.document.getBaseURI() : null;
        this.startLine = startLine > 0 ? startLine : this.node != null ? this.node.startLine() : 0;
        this.endLine = endLine > 0 ? endLine : this.node != null ? this.node.endLine() : 0;
        this.column = column > 0 ? column : this.node != null ? this.node.column() : 0;
    }

    public InvalidXMLException(String message, @Nullable Node node, Throwable cause) {
        this(message, node, null, null, 0, 0, 0, cause);
    }

    public InvalidXMLException(String message, @Nullable Document document, Throwable cause) {
        this(message, null, document, null, 0, 0, 0, cause);
    }

    public InvalidXMLException(String message, @Nullable String documentPath, Throwable cause) {
        this(message, null, null, documentPath, 0, 0, 0, cause);
    }

    public InvalidXMLException(String message, @Nullable Element element, Throwable cause) {
        this(message, element == null ? null : new Node(element), cause);
    }

    public InvalidXMLException(String message, @Nullable Attribute attribute, Throwable cause) {
        this(message, attribute == null ? null : new Node(attribute), cause);
    }

    public InvalidXMLException(String message, Document document) {
        this(message, document, null);
    }

    public InvalidXMLException(String message, String documentPath) {
        this(message, documentPath, null);
    }

    public InvalidXMLException(String message, Node node) {
        this(message, node, null);
    }

    public InvalidXMLException(String message, Element element) {
        this(message, element, null);
    }

    public InvalidXMLException(String message, Attribute attribute) {
        this(message, attribute, null);
    }

    public InvalidXMLException(String message) {
        this(message, (Node) null);
    }

    public static InvalidXMLException fromJDOM(JDOMParseException e, String documentPath) {
        return new InvalidXMLException(e.getMessage(), null, e.getPartialDocument(), documentPath, e.getLineNumber(), e.getLineNumber(), e.getColumnNumber(), e);
    }

    public static <T> T offeringNode(@Nullable Node node, ThrowingSupplier<T, ? extends InvalidXMLException> block) throws InvalidXMLException {
        try {
            return block.getThrows();
        } catch(InvalidXMLException e) {
            e.offerNode(node);
            throw e;
        }
    }

    public @Nullable Node getNode() {
        return node;
    }

    public void setNode(@Nullable Node node) {
        this.node = node;

        if(node == null) {
            document = null;
            documentPath = null;
            startLine = endLine = column = 0;
        } else {
            document = node.getDocument();
            documentPath = document == null ? null : document.getBaseURI();
            startLine = node.startLine();
            endLine = node.endLine();
            column = node.column();
        }
    }

    public void offerNode(@Nullable Node node) {
        if(this.node == null) {
            setNode(node);
        }
    }

    public @Nullable Document getDocument() {
        return document;
    }

    public @Nullable String getDocumentPath() {
        return documentPath;
    }

    public @Nullable String getWhere() {
        return Node.describeLocation(startLine, endLine, column).orElse(null);
    }

    public @Nullable String getWhatAndWhere() {
        String what = getNode() == null ? null : getNode().describe();
        String where = getWhere();

        if(what != null) {
            if(where != null) {
                return what + " @ " + where;
            } else {
                return what;
            }
        } else {
            if(where != null) {
                return where;
            } else {
                return null;
            }
        }
    }

    public @Nullable String getFullLocation() {
        String path = getDocumentPath();
        String location = getWhatAndWhere();
        if(path != null) {
            return location == null ? path : path + " - " + location;
        } else {
            return location == null ? null : location;
        }
    }
}
