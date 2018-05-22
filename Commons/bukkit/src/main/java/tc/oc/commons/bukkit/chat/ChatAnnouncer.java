package tc.oc.commons.bukkit.chat;

import tc.oc.api.docs.Chat;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.message.MessageListener;
import tc.oc.api.message.types.ModelUpdate;
import tc.oc.api.queue.PrimaryQueue;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.broadcast.BroadcastSender;
import tc.oc.commons.bukkit.channels.Channel;
import tc.oc.commons.bukkit.channels.ChannelRouter;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.scheduler.MainThreadExecutor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class ChatAnnouncer implements PluginFacet, MessageListener {

    private final Server server;
    private final ServerStore serverStore;
    private final PrimaryQueue primaryQueue;
    private final MainThreadExecutor executor;
    private final ChannelRouter channelRouter;
    private final BroadcastSender broadcaster;

    @Inject ChatAnnouncer(Server server, ServerStore serverStore, PrimaryQueue primaryQueue, MainThreadExecutor executor, ChannelRouter channelRouter, BroadcastSender broadcaster) {
        this.server = server;
        this.primaryQueue = primaryQueue;
        this.executor = executor;
        this.channelRouter = channelRouter;
        this.broadcaster = broadcaster;
        this.serverStore = serverStore;
    }

    @Override
    public void enable() {
        primaryQueue.bind(ModelUpdate.class);
        primaryQueue.subscribe(this, executor);
    }

    @Override
    public void disable() {
        primaryQueue.unsubscribe(this);
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
                return remote;
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
