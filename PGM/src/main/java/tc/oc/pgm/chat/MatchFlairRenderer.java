package tc.oc.pgm.chat;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.md_5.bungee.api.ChatColor;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.commons.bukkit.flairs.FlairConfiguration;
import tc.oc.commons.bukkit.flairs.FlairRenderer;
import tc.oc.commons.bukkit.chat.NameFlag;
import tc.oc.commons.bukkit.chat.NameType;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchManager;

/**
 * Add mapmaker flair
 */
@Singleton
public class MatchFlairRenderer extends FlairRenderer {

    private static final String MAPMAKER_FLAIR_LEGACY = ChatColor.BLUE + "*";

    private final MatchManager matchManager;

    @Inject MatchFlairRenderer(MinecraftService minecraftService, BukkitUserStore userStore, MatchManager matchManager, FlairConfiguration flairConfiguration) {
        super(minecraftService, userStore, flairConfiguration);
        this.matchManager = matchManager;
    }

    @Override
    public String getLegacyName(Identity identity, NameType type) {
        String name = super.getLegacyName(identity, type);

        if(!type.style.contains(NameFlag.MAPMAKER)) return name;

        // If we ever have multiple simulataneous matches, the mapmaker flair will show
        // in all matches, not just the one for the player's map. We can't avoid this
        // without some way to render names differently in each match (which we could do).
        for(Match match : matchManager.currentMatches()) {
            if(!match.isUnloaded() && match.getMap().getInfo().isAuthor(identity.getPlayerId())) {
                name = MAPMAKER_FLAIR_LEGACY + name;
                break;
            }
        }

        return name;
    }
}
