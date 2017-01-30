package tc.oc.api.docs;

import tc.oc.api.annotations.Serialize;

import static com.google.common.base.Preconditions.checkNotNull;

public class SimpleUserId implements UserId {

    @Serialize private String player_id;

    public SimpleUserId(String player_id) {
        this.player_id = checkNotNull(player_id);
    }

    /** For deserialization only */
    protected SimpleUserId() {
        this.player_id = null;
    }

    public static SimpleUserId copyOf(UserId userId) {
        return userId.getClass().equals(SimpleUserId.class) ? (SimpleUserId) userId
                                                            : new SimpleUserId(userId.player_id());
    }

    @Serialize
    @Override
    public String player_id() {
        return this.player_id;
    }

    @Override
    final public boolean equals(Object obj) {
        return obj instanceof UserId && ((UserId) obj).player_id().equals(this.player_id());
    }

    @Override
    final public int hashCode() {
        return this.player_id().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "{player_id=" + player_id() + "}";
    }
}
