package tc.oc.parse;

public class MissingException extends FormatException {

    public MissingException(String type, String name) {
        super("Missing required " + type + " '" + name + "'");
    }
}
