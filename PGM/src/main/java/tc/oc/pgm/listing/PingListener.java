package tc.oc.pgm.listing;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import tc.oc.minecraft.api.event.Enableable;
import tc.oc.pgm.match.Match;

@Singleton
public class PingListener implements Listener, Enableable {

    private final Provider<Match> matchProvider;
    private final ListingService listingService;

    @Inject PingListener(Provider<Match> matchProvider, ListingService listingService) {
        this.matchProvider = matchProvider;
        this.listingService = listingService;
    }

    @EventHandler
    private void onPing(ServerListPingEvent event) {
        event.getExtra().put("pgm", new Info());
    }

    private class Info {
        private class Map {
            final String name;
            final @Nullable String icon;

            private Map(String name, String icon) {
                this.name = name;
                this.icon = icon;
            }
        }

        final @Nullable String session = listingService.sessionDigest();
        final Map map;
        final int participants;
        final int observers;

        Info() {
            final Match match = matchProvider.get();
            this.map = new Map(match.getMap().getName(),
                               match.getMap().getThumbnailUri().orElse(null));
            this.participants = match.getParticipatingPlayers().size();
            this.observers = match.getObservingPlayers().size();
        }
    }
}
