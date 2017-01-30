package tc.oc.api.exceptions;

public class UnmappedUserException extends IllegalStateException {
    private final String username;

    public UnmappedUserException(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getMessage() {
        return "No UserId stored for username \"" + this.username + "\"";
    }
}
