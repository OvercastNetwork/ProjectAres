package tc.oc.pgm.xml.parser;

import javax.annotation.Nullable;

import com.google.common.reflect.TypeToken;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public abstract class TransfiniteParser<T> extends PrimitiveParser<T> {

    protected TransfiniteParser(@Nullable TypeToken<T> type) {
        super(type);
    }

    protected TransfiniteParser() {}

    protected abstract T infinity(boolean sign);

    protected abstract T parseFinite(Node node, String text) throws FormatException, InvalidXMLException;

    @Override
    public T parseInternal(Node node, String text) throws FormatException, InvalidXMLException {
        if("oo".equals(text)) {
            return infinity(true);
        } else if("-oo".equals(text)) {
            return infinity(false);
        } else {
            return parseFinite(node, text);
        }
    }
}
