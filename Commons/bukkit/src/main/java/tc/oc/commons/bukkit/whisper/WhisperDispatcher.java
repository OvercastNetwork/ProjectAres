package tc.oc.commons.bukkit.whisper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.BasicModel;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.Whisper;
import tc.oc.api.docs.virtual.WhisperDoc;
import tc.oc.api.message.MessageListener;
import tc.oc.api.message.MessageService;
import tc.oc.api.message.types.ModelUpdate;
import tc.oc.api.model.IdFactory;
import tc.oc.api.users.UserService;
import tc.oc.api.whispers.WhisperService;
import tc.oc.commons.bukkit.event.UserLoginEvent;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.settings.SettingManagerProvider;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.scheduler.Scheduler;
import tc.oc.minecraft.scheduler.MainThreadExecutor;

public class WhisperDispatcher implements WhisperSender, Listener, MessageListener, PluginFacet {

    // Mark a whisper as read if the player is still online
    // this long after first seeing it. This prevents a player
    // from missing a message because they disconnected right
    // at the same time they received it.
    private static final Duration MARK_READ_DELAY = Duration.ofSeconds(3);

    private final MessageService queue;
    private final OnlinePlayers onlinePlayers;
    private final MainThreadExecutor executor;
    private final Scheduler scheduler;
    private final WhisperService whisperService;
    private final WhisperFormatter formatter;
    private final Server localServer;
    private final IdFactory idFactory;
    private final SettingManagerProvider playerSettings;
    private final UserService userService;

    @Inject WhisperDispatcher(MessageService queue,
                              OnlinePlayers onlinePlayers,
                              MainThreadExecutor executor,
                              Scheduler scheduler,
                              WhisperService whisperService,
                              WhisperFormatter formatter,
                              Server localServer,
                              IdFactory idFactory,
                              SettingManagerProvider playerSettings,
                              UserService userService) {
        this.queue = queue;
        this.onlinePlayers = onlinePlayers;
        this.executor = executor;
        this.scheduler = scheduler;
        this.whisperService = whisperService;
        this.formatter = formatter;
        this.localServer = localServer;
        this.idFactory = idFactory;
        this.playerSettings = playerSettings;
        this.userService = userService;
    }

    @Override
    public void enable() {
        queue.bind(ModelUpdate.class);
        queue.subscribe(this, executor);
    }

    @Override
    public void disable() {
        queue.unsubscribe(this);
    }

    @Override
    public void send(CommandSender sender, Identity from, Identity to, String content) {
        executor.callback(userService.find(to.getPlayerId()), user -> {
            final WhisperSettings.Options setting = (WhisperSettings.Options) playerSettings.getManager(user).getValue(WhisperSettings.receive());
            if(setting.canSend(sender, to)) {
                final ListenableFuture<Whisper> future = whisperService.update(new Out(from, to, content));
                executor.callback(future, whisper -> formatter.send(sender, whisper));
            } else {
                formatter.blocked(sender, to);
            }
        });
    }

    private void markRead(Player player, Runnable block) {
        scheduler.createDelayedTask(MARK_READ_DELAY, () -> {
            // Once willBeOnline goes false, it stays false forever,
            // whereas isOnline can become true again if the player
            // reconnects.
            if(player.willBeOnline()) {
                block.run();
            }
        });
    }

    @HandleMessage
    private void receive(ModelUpdate<Whisper> update) {
        final Whisper whisper = update.document();
        if(whisper.delivered()) return;

        onlinePlayers.byUserId(whisper.recipient_uid()).ifPresent(player -> {
            formatter.receive(player, whisper);
            markRead(player, () ->
                whisperService.update(new Ack(whisper._id()))
            );
        });
    }

    /**
     * Make sure this runs after the welcome message in the lobby
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void login(UserLoginEvent event) {
        final List<Whisper> whispers = event.response().whispers();
        if(!whispers.isEmpty()) {
            final Player player = event.getPlayer();
            whispers.forEach(whisper -> formatter.receive(player, whisper));
            markRead(player, () ->
                whisperService.updateMulti(Lists.transform(whispers, whisper -> new Ack(whisper._id())))
            );
        }
    }

    private class Out extends BasicModel implements Whisper {

        final Identity from, to;
        final String content;

        private Out(Identity from, Identity to, String content) {
            super(idFactory.newId());
            this.from = from;
            this.to = to;
            this.content = content;
        }

        @Override public String family() {
            return localServer.family();
        }

        @Override public String server_id() {
            return localServer._id();
        }

        @Override public Instant sent() {
            return Instant.now();
        }

        @Override public boolean delivered() {
            return false;
        }

        @Override public PlayerId sender_uid() {
            return from.getPlayerId();
        }

        @Override public String sender_nickname() {
            return from.getNickname();
        }

        @Override public PlayerId recipient_uid() {
            return to.getPlayerId();
        }

        @Override public String recipient_specified() {
            return to.getNickname();
        }

        @Override public String content() {
            return content;
        }
    }

    private class Ack extends BasicModel implements WhisperDoc.Delivery {
        public Ack(String _id) {
            super(_id);
        }

        @Override
        public boolean delivered() {
            return true;
        }
    }
}
