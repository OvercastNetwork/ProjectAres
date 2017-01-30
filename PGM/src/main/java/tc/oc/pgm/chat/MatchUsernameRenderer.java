package tc.oc.pgm.chat;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.md_5.bungee.api.ChatColor;
import tc.oc.commons.bukkit.chat.NameFlag;
import tc.oc.commons.bukkit.chat.NameType;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.nick.UsernameRenderer;
import tc.oc.commons.core.chat.ChatUtils;
import tc.oc.pgm.match.MatchManager;
import tc.oc.pgm.match.MatchPlayer;

/**
 * Adds team colors to player names
 */
@Singleton
public class MatchUsernameRenderer extends UsernameRenderer {

    private final MatchManager matchManager;

    @Inject MatchUsernameRenderer(MatchManager matchManager) {
        this.matchManager = matchManager;
    }

    @Override
    public ChatColor getColor(Identity identity, NameType type) {
        if(type.online && !(type.dead && type.style.contains(NameFlag.DEATH))) {
            MatchPlayer player = matchManager.getPlayer(identity.getPlayerId());
            if(player != null) {
                return ChatUtils.convert(player.getParty().getColor());
            }
        }

        return super.getColor(identity, type);
    }
}
