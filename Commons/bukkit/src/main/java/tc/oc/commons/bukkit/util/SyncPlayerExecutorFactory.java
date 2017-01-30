package tc.oc.commons.bukkit.util;

import java.util.UUID;
import java.util.concurrent.Executor;
import javax.inject.Inject;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.SimpleUserId;
import tc.oc.api.docs.UserId;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.minecraft.scheduler.MainThreadExecutor;
import tc.oc.commons.core.concurrent.ContextualExecutor;
import tc.oc.commons.core.concurrent.ContextualExecutorImpl;

/**
 * Creates {@link ContextualExecutor}s for {@link Player}s that will only execute
 * tasks when the player is online. If the same player reconnects, their current
 * {@link Player} instance will always be passed to the tasks.
 */
public class SyncPlayerExecutorFactory {

    private final MainThreadExecutor mainThreadExecutor;
    private final SyncExecutor syncExecutor;
    private final OnlinePlayers onlinePlayers;

    @Inject SyncPlayerExecutorFactory(MainThreadExecutor mainThreadExecutor, SyncExecutor syncExecutor, OnlinePlayers onlinePlayers) {
        this.mainThreadExecutor = mainThreadExecutor;
        this.syncExecutor = syncExecutor;
        this.onlinePlayers = onlinePlayers;
    }

    public <T extends CommandSender> ContextualExecutor<CommandSender> mainThread(T sender) {
        return create(sender, mainThreadExecutor);
    }

    public ContextualExecutor<Player> mainThread(Player player) {
        return create(player, mainThreadExecutor);
    }

    public ContextualExecutor<Player> mainThread(UserId userId) {
        return create(userId, mainThreadExecutor);
    }

    public <T extends CommandSender> ContextualExecutor<CommandSender> queued(T sender) {
        return create(sender, syncExecutor);
    }

    public ContextualExecutor<Player> queued(Player player) {
        return create(player, syncExecutor);
    }

    public ContextualExecutor<Player> queued(UserId userId) {
        return create(userId, syncExecutor);
    }

    public <T extends CommandSender> ContextualExecutor<T> create(T sender, Executor executor) {
        if(sender instanceof Player) {
            return (ContextualExecutor<T>) create((Player) sender, executor);
        } else {
            return new ContextualExecutorImpl<>(() -> sender, executor);
        }
    }

    public ContextualExecutor<Player> create(Player player, Executor executor) {
        final UUID uuid = player.getUniqueId();
        return new ContextualExecutorImpl<>(() -> onlinePlayers.find(uuid), executor);
    }

    public ContextualExecutor<Player> create(UserId userId, Executor executor) {
        final UserId simpleUserId = SimpleUserId.copyOf(userId);
        return new ContextualExecutorImpl<>(() -> onlinePlayers.find(simpleUserId), executor);
    }
}