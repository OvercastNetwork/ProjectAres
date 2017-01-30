package tc.oc.pgm.chat;

import javax.inject.Inject;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.chat.CachingNameRenderer;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.bukkit.nick.PlayerAppearanceChanger;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.pgm.events.MatchLoadEvent;
import tc.oc.pgm.events.MatchUnloadEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.map.Contributor;
import tc.oc.pgm.match.Match;

public class MatchNameInvalidator implements Listener, PluginFacet {

    private final PlayerAppearanceChanger playerAppearanceChanger;
    private final CachingNameRenderer cachingNameRenderer;
    private final IdentityProvider identityProvider;

    @Inject MatchNameInvalidator(PlayerAppearanceChanger playerAppearanceChanger, CachingNameRenderer cachingNameRenderer, IdentityProvider identityProvider) {
        this.playerAppearanceChanger = playerAppearanceChanger;
        this.cachingNameRenderer = cachingNameRenderer;
        this.identityProvider = identityProvider;
    }

    // Invalidate names on team change so the color can update.
    // Dead players have a different NameType than living ones, so invalidation is not needed for that.

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPartyChange(PlayerPartyChangeEvent event) {
        playerAppearanceChanger.refreshPlayer(event.getPlayer().getBukkit());
        cachingNameRenderer.invalidateCache(identityProvider.currentIdentity(event.getPlayer().getBukkit()));
    }

    // Invalidate the names of mapmakers when their map loads/unloads,
    // so the mapmaker flair can be added/removed.

    private void invalidateMapmakers(Match match) {
        match.getMap().getInfo().authors.stream()
            .filter((c) -> c.getUser() != null)
            .map(Contributor::getIdentity)
            .forEach((identity) -> {
                cachingNameRenderer.invalidateCache(identity);
                final Player player = identity.getPlayer();
                if(player != null) playerAppearanceChanger.refreshPlayer(player);
            });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMatchLoad(MatchLoadEvent event) {
        invalidateMapmakers(event.getMatch());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMatchUnload(MatchUnloadEvent event) {
        invalidateMapmakers(event.getMatch());
    }
}
