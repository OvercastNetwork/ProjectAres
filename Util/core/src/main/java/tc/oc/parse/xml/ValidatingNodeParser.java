package tc.oc.parse.xml;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.w3c.dom.Node;
import tc.oc.parse.ParseException;
import tc.oc.parse.validate.Validation;

public class ValidatingNodeParser<T> implements NodeParser<T> {

    private final NodeParser<T> parser;
    private final List<Validation<? super T>> validations;

    public ValidatingNodeParser(NodeParser<T> parser, List<Validation<? super T>> validations) {
        this.parser = parser;
        this.validations = validations;
    }

    public ValidatingNodeParser(NodeParser<T> parser, Validation<? super T>... validations) {
        this(parser, ImmutableList.copyOf(validations));
    }

    @Override
    public TypeToken<T> paramToken() {
        return parser.paramToken();
    }

    @Override
    public T parse(Node node) throws ParseException {
        final T value = parser.parse(node);
        for(Validation<? super T> v : validations) {
            v.validate(value);
        }
        return value;
    }
}
