package tc.oc.api.docs;

import tc.oc.api.annotations.Serialize;

import static com.google.common.base.Preconditions.checkNotNull;

public class SimplePlayerId extends SimpleUserId implements PlayerId {

    @Serialize private String _id;
    @Serialize private String username;

    public SimplePlayerId(String _id, String player_id, String username) {
        super(player_id);
        this._id = checkNotNull(_id);
        this.username = checkNotNull(username);
    }

    public SimplePlayerId(PlayerId playerId) {
        this(playerId._id(), playerId.player_id(), playerId.username());
    }

    /** For deserialization only */
    protected SimplePlayerId() {
        super();
        this._id = this.username = null;
    }

    /**
     * Return a {@link SimplePlayerId} equal to the given {@link PlayerId}
     */
    public static SimplePlayerId copyOf(PlayerId playerId) {
        return playerId.getClass().equals(SimplePlayerId.class) ? (SimplePlayerId) playerId
                                                                : new SimplePlayerId(playerId);
    }

    @Serialize
    @Override
    public String _id() {
        return _id;
    }

    @Serialize
    @Override
    public String username() {
        return username;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "{_id=" + _id() +
               " player_id=" + player_id() +
               " username=" + username() +
               "}";
    }
}
