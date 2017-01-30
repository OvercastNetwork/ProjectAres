package tc.oc.api.minecraft.users;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.User;
import tc.oc.api.docs.UserId;
import tc.oc.minecraft.api.entity.Player;

/**
 * Enumerate currently online players, or look them up by various criteria.
 *
 * The {@link #find} methods return null if the user is not online,
 * while the {@link #get} methods throw {@link IllegalStateException}.
 */
public interface OnlinePlayers<P extends Player> {

    Collection<P> all();

    default Stream<P> stream() {
        return all().stream();
    }

    default int count() {
        return all().size();
    }

    @Nullable P find(String name);
    @Nullable P find(UUID uuid);
    @Nullable P find(UserId userId);

    default Optional<P> byName(String name) {
        return Optional.ofNullable(find(name));
    }

    default Optional<P> byUuid(UUID uuid) {
        return Optional.ofNullable(find(uuid));
    }

    default Optional<P> byUserId(UserId userId) {
        return Optional.ofNullable(find(userId));
    }

    default P get(String name) {
        final P player = find(name);
        if(player == null) throw new IllegalStateException("Player with username " + name + " is not online");
        return player;
    }

    default P get(UUID uuid) {
        final P player = find(uuid);
        if(player == null) throw new IllegalStateException("Player with UUID " + uuid + " is not online");
        return player;
    }

    default P get(UserId userId) {
        final P player = find(userId);
        if(player == null) throw new IllegalStateException("Player with UserId " + userId.player_id() + " is not online");
        return player;
    }
}
