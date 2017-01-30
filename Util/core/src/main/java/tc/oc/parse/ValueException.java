package tc.oc.parse;

public class ValueException extends ParseException {

    public ValueException() {
    }

    public ValueException(String message) {
        super(message);
    }

    public ValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValueException(Throwable cause) {
        super(cause);
    }
}
