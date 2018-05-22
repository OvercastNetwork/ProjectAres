package tc.oc.commons.bukkit.chat;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Chat;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.model.BatchUpdater;
import tc.oc.api.model.BatchUpdaterFactory;
import tc.oc.api.model.IdFactory;
import tc.oc.api.model.ModelService;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.api.event.Listener;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

@Singleton
public class ChatCreator implements PluginFacet, Listener {

    private final IdFactory idFactory;
    private final ModelService<Chat, ChatDoc.Partial> chatService;
    private final BatchUpdater<ChatDoc.Partial> chatBatchUpdater;
    private final Server server;

    @Inject ChatCreator(IdFactory idFactory, ModelService<Chat, ChatDoc.Partial> chatService, BatchUpdaterFactory<ChatDoc.Partial> chatBatchUpdaterFactory, Server server) {
        this.idFactory = idFactory;
        this.chatService = chatService;
        this.chatBatchUpdater = chatBatchUpdaterFactory.createBatchUpdater(Duration.ofMinutes(1));
        this.server = server;
    }

    public ListenableFuture<Chat> chat(@Nullable PlayerId sender, String message, ChatDoc.Type type, Consumer<Chat> callback) {
        return send(sender, message, type, null, callback);
    }

    public ListenableFuture<Chat> broadcast(@Nullable PlayerId sender, String message, ChatDoc.Destination destination, String destination_id) {
        return send(sender, message, ChatDoc.Type.BROADCAST,
            new ChatDoc.Broadcast() {
                public ChatDoc.Destination destination() { return destination; }
                public String id() { return destination_id; }
            }, null
        );
    }

    protected ListenableFuture<Chat> send(@Nullable PlayerId sender, String message, ChatDoc.Type type, @Nullable ChatDoc.Broadcast broadcast, @Nullable Consumer<Chat> callback) {
        final Instant time = Instant.now();
        final String id = idFactory.newId();
        ChatDoc.Creation chat = new ChatDoc.Creation() {
            public String _id() { return id; }
            public String sender_id() { return sender != null ? sender._id() : null; }
            public String message() { return message; }
            public String server_id() { return server._id(); }
            public String match_id() { return ofNullable(server.current_match()).map(MatchDoc::_id).orElse(null); }
            public ChatDoc.Type type() { return type; }
            public Instant sent_at() { return time; }
            public ChatDoc.Broadcast broadcast() { return broadcast; }
        };
        // Some chats are only consumed by the local server,
        // so those messages can have delayed reporting to the API.
        if(type.batchUpdate) {
            if(callback != null) callback.accept(mock(sender, chat));
            chatBatchUpdater.update(chat);
            return Futures.immediateFuture(null);
        } else {
            return chatService.update(chat);
        }
    }

    private Chat mock(@Nullable PlayerId sender, ChatDoc.Creation chat) {
        return new Chat() {
            public PlayerId sender() { return sender; }
            public String _id() { return chat._id(); }
            public String message() { return chat.message(); }
            public String server_id() { return chat.server_id(); }
            public String match_id() { return chat.match_id(); }
            public ChatDoc.Type type() { return chat.type(); }
            public Instant sent_at() { return chat.sent_at(); }
            public ChatDoc.Broadcast broadcast() { return chat.broadcast(); }
        };
    }

}
