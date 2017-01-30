package tc.oc.pgm.xml.validate;

import java.util.regex.Pattern;

import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class NonBlank implements Validation<String> {

    private static final Pattern PATTERN = Pattern.compile("\\S");

    @Override
    public void validate(String value, Node node) throws InvalidXMLException {
        if(value == null || !PATTERN.matcher(value).find()) {
            throw new InvalidXMLException("cannot be blank", node);
        }
    }
}
