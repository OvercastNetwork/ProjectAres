package tc.oc.commons.core.inspect;

public class InspectionException extends Exception {
    public InspectionException(String message) {
        super(message);
    }

    public InspectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
