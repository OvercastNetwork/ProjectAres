package tc.oc.commons.bukkit.trophies;

import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.ListenableFuture;
import org.bukkit.entity.Player;
import org.bukkit.event.EventBus;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.Trophy;
import tc.oc.api.docs.User;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.api.trophies.TrophyStore;
import tc.oc.api.users.UserService;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Streams;

import static com.google.common.util.concurrent.Futures.immediateFuture;
import static tc.oc.commons.core.concurrent.FutureUtils.mapAsync;
import static tc.oc.commons.core.concurrent.FutureUtils.mapSync;

/**
 * Handles listing, granting, and revoking of {@link Trophy}s from {@link User}s.
 */
@Singleton
public class TrophyCase {

    private final SyncExecutor syncExecutor;
    private final TrophyStore trophyStore;
    private final BukkitUserStore userStore;
    private final UserService userService;
    private final EventBus eventBus;

    @Inject TrophyCase(SyncExecutor syncExecutor, TrophyStore trophyStore, BukkitUserStore userStore, UserService userService, EventBus eventBus) {
        this.syncExecutor = syncExecutor;
        this.trophyStore = trophyStore;
        this.userStore = userStore;
        this.userService = userService;
        this.eventBus = eventBus;
    }

    public ListenableFuture<Set<Trophy>> getTrophies(UserId userId) {
        return mapSync(userService.find(userId), user -> user.trophy_ids()
                                                             .stream()
                                                             .map(trophyStore::byId)
                                                             .collect(Collectors.toImmutableSet()));
    }

    public boolean hasTrophy(User user, Trophy trophy) {
        return user.trophy_ids().contains(trophy._id());
    }

    public boolean hasTrophy(Player player, Trophy trophy) {
        return hasTrophy(userStore.getUser(player), trophy);
    }

    public ListenableFuture<Boolean> hasTrophy(UserId userId, Trophy trophy) {
        return mapSync(userService.find(userId), user -> hasTrophy(user, trophy));
    }

    public ListenableFuture<Boolean> giveTrophy(UserId userId, Trophy trophy) {
        return grantOrRevoke(userId, trophy, true);
    }

    public ListenableFuture<Boolean> revokeTrophy(UserId userId, Trophy trophy) {
        return grantOrRevoke(userId, trophy, false);
    }

    public ListenableFuture<Boolean> grantOrRevoke(UserId userId, Trophy trophy, boolean grant) {
        return mapAsync(userService.find(userId), user -> {
            if(grant == user.trophy_ids().contains(trophy._id())) {
                return immediateFuture(false);
            }
            final List<String> trophyIds = (grant ? Streams.append(user.trophy_ids().stream(), trophy._id())
                                                  : Streams.remove(user.trophy_ids().stream(), trophy._id()))
                .collect(Collectors.toImmutableList());

            final ListenableFuture<User> future = userService.update(user, (UserDoc.Trophies) () -> trophyIds);
            future.addListener(() -> eventBus.callEvent(new TrophyEvent(user, trophy, grant)), syncExecutor);
            return mapSync(future, u -> true);
        });
    }
}
