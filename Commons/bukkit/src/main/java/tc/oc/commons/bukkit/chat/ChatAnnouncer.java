package tc.oc.commons.bukkit.chat;

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import tc.oc.api.docs.Chat;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.message.MessageListener;
import tc.oc.api.message.MessageQueue;
import tc.oc.api.message.types.ModelUpdate;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.broadcast.BroadcastSender;
import tc.oc.commons.bukkit.channels.Channel;
import tc.oc.commons.bukkit.channels.ChannelConfiguration;
import tc.oc.commons.bukkit.channels.ChannelRouter;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.scheduler.MainThreadExecutor;

@Singleton
public class ChatAnnouncer implements PluginFacet, MessageListener {

    private final ChannelConfiguration configuration;
    private final Server server;
    private final ServerStore serverStore;
    private final MessageQueue queue;
    private final MainThreadExecutor executor;
    private final ChannelRouter channelRouter;
    private final BroadcastSender broadcaster;

    @Inject
    public ChatAnnouncer(ChannelConfiguration configuration, Server server, ServerStore serverStore,
                         MessageQueue queue, MainThreadExecutor executor, ChannelRouter channelRouter,
                         BroadcastSender broadcaster) {
        this.configuration = configuration;
        this.server = server;
        this.serverStore = serverStore;
        this.queue = queue;
        this.executor = executor;
        this.channelRouter = channelRouter;
        this.broadcaster = broadcaster;
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

    @MessageListener.HandleMessage
    public void onChat(ModelUpdate<Chat> message) {
        final Chat chat = message.document();
        if(shouldAnnounce(chat)) {
            final ChatDoc.Type type = chat.type();
            final Optional<Channel> channel = channelRouter.getChannel(chat);
            if(channel.isPresent()) {
                channel.get().show(chat);
            } else if(type == ChatDoc.Type.BROADCAST) {
                broadcaster.show(chat);
            }
        }
    }

    public boolean shouldAnnounce(Chat chat) {
        final boolean remote = serverStore.canCommunicate(server._id(), chat.server_id());
        switch(chat.type()) {
            case SERVER:
            case TEAM:
                return false;
            case ADMIN:
                if (!configuration.admin_cross_server() && !chat.server_id().equalsIgnoreCase(server._id())) {
                    return false;
                }
                return configuration.admin_enabled() && remote;
            case BROADCAST:
                return shouldAnnounce(chat.broadcast());
        }
        return false;
    }

    public boolean shouldAnnounce(ChatDoc.Broadcast broadcast) {
        final String destination = broadcast.id();
        switch(broadcast.destination()) {
            case SERVER:
                return server._id().equalsIgnoreCase(destination);
            case FAMILY:
                final String family = server.family();
                return family == null || family.equalsIgnoreCase(destination);
            case GAME:
                final String game = server.game_id();
                return game == null || game.equalsIgnoreCase(destination);
            case NETWORK:
                final ServerDoc.Network network = server.network();
                return network == null || network.name().equalsIgnoreCase(destination);
            case GLOBAL:
                return true;
        }
        return false;
    }

}
