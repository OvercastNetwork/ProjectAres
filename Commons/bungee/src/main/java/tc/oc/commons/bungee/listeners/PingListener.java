package tc.oc.commons.bungee.listeners;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc.Banner;
import tc.oc.api.model.ModelSync;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bungee.servers.LobbyTracker;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.protocol.MinecraftVersion;

import static tc.oc.commons.core.stream.Collectors.toImmutableSet;

@Singleton
public class PingListener implements Listener, PluginFacet {
    private static final int MAX_PLAYERS = 300;

    private final Logger logger;
    private final Random random = new Random();
    private final Plugin plugin;
    private final Server localServer;
    private final ServerStore serverStore;
    private final ExecutorService executor;
    private final LobbyTracker lobbyTracker;

    @Inject PingListener(Loggers loggers, Plugin plugin, Server localServer, ServerStore serverStore, LobbyTracker lobbyTracker, @ModelSync ExecutorService executor) {
        this.plugin = plugin;
        this.localServer = localServer;
        this.serverStore = serverStore;
        this.executor = executor;
        this.lobbyTracker = lobbyTracker;
        this.logger = loggers.get(getClass());
    }

    private Banner chooseBanner() {
        List<Banner> banners = localServer.banners();
        if(banners.isEmpty()) return null;

        int totalWeight = 0;
        for(Banner banner : banners) totalWeight += banner.weight();
        int rando = random.nextInt(totalWeight);

        for(Banner banner : banners) {
            rando -= banner.weight();
            if(rando < 0) return banner;
        }

        return null;
    }


    @EventHandler
    public void onPing(final ProxyPingEvent event) {
        event.registerIntent(plugin);

        final int proto = event.getConnection().getVersion();
        final Set<Integer> supported = lobbyTracker.supportedProtocols().collect(toImmutableSet());

        if(!supported.isEmpty() && !supported.contains(proto)) {
            event.getResponse().setVersion(new ServerPing.Protocol(
                new Component("Connect with ", ChatColor.RED)
                    .extra(describeVersionRange(supported))
                    .toLegacyText(),
                -1
            ));
        }

        executor.execute(() -> {
            event.getResponse().setPlayers(new ServerPing.Players(MAX_PLAYERS, serverStore.countBukkitPlayers(), null));
            final Banner banner = chooseBanner();
            if(banner != null) event.getResponse().setDescription(banner.rendered());
            event.completeIntent(plugin);
        });
    }

    public static BaseComponent describeVersionRange(Collection<Integer> protos) {
        final Set<MinecraftVersion> versions = protos.stream()
                                                     .map(MinecraftVersion::byProtocol)
                                                     .filter(v -> v != null)
                                                     .collect(toImmutableSet());
        final MinecraftVersion oldest = Collections.min(versions);
        final MinecraftVersion newest = Collections.max(versions);
        final Component c = new Component(oldest.version(), ChatColor.AQUA);
        if(oldest != newest) {
            c.extra(" to ", ChatColor.RED)
             .extra(newest.version());
        }
        return c;
    }
}
