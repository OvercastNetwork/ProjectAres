package tc.oc.api.exceptions;

/**
 * Thrown when you try to do something that requires an API connection,
 * but the API is not currently connected.
 */
public class ApiNotConnected extends IllegalStateException {

    public ApiNotConnected() {
        this("No API connection");
    }

    public ApiNotConnected(String s) {
        super(s);
    }
}
