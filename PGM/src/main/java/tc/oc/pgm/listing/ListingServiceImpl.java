package tc.oc.pgm.listing;

import java.security.SecureRandom;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.ListenableFuture;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import tc.oc.api.http.HttpClient;
import tc.oc.api.http.HttpOption;
import tc.oc.api.message.types.Reply;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.concurrent.Flexecutor;
import tc.oc.minecraft.api.event.Enableable;
import tc.oc.minecraft.api.server.LocalServer;
import tc.oc.minecraft.scheduler.Sync;

@Singleton
class ListingServiceImpl implements ListingService, Enableable {

    private final HttpClient http;
    private final ListingConfiguration config;
    private final LocalServer localServer;
    private final SecureRandom random = new SecureRandom();
    private final Flexecutor executor;
    private final Audiences audiences;
    private final ConsoleCommandSender console;

    private boolean online;
    private @Nullable String sessionId;
    private @Nullable String sessionDigest;

    @Inject ListingServiceImpl(HttpClient http, ListingConfiguration config, LocalServer localServer, @Sync(defer = true) Flexecutor executor, Audiences audiences, ConsoleCommandSender console) {
        this.http = http;
        this.config = config;
        this.localServer = localServer;
        this.executor = executor;
        this.audiences = audiences;
        this.console = console;
    }

    @Override
    public @Nullable String sessionDigest() {
        return sessionDigest;
    }

    @Override
    public void enable() {
        if(config.enabled()) {
            // Don't announce until we are ready to receive the ping
            executor.execute(() -> update(true));
        }
    }

    @Override
    public void disable() {
        if(online) {
            update(false);
        }
    }

    @Override
    public ListenableFuture<Reply> update(boolean online) {
        return update(online, console);
    }

    @Override
    public ListenableFuture<Reply> update(boolean online, CommandSender sender) {
        this.online = online;

        if(sessionId == null) {
            final byte[] bytes = new byte[20];
            random.nextBytes(bytes);
            sessionId = Hex.encodeHexString(bytes);
            sessionDigest = DigestUtils.sha1Hex(sessionId);
        }

        final ListenableFuture<Reply> future = http.post(config.announceUrl().toString(), new ListingUpdate() {
            @Override public @Nullable String host() {
                return config.serverHost();
            }

            @Override
            public int port() {
                return config.serverPort().orElseGet(localServer::getPort);
            }

            @Override
            public boolean online() {
                return online;
            }

            @Override
            public String session() {
                return sessionId;
            }
        }, Reply.class, HttpOption.INFINITE_RETRY);

        executor.callback(
            future,
            CommandFutureCallback.onSuccess(sender, reply -> {
                if(!online) {
                    sessionId = sessionDigest = null;
                }

                audiences.get(sender).sendMessage(new TranslatableComponent(online ? "announce.online" : "announce.offline"));
            })
        );

        return future;
    }
}
