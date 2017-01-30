package tc.oc.commons.bukkit.nick;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.Skin;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import tc.oc.commons.bukkit.chat.FlairRenderer;
import tc.oc.commons.bukkit.chat.FullNameRenderer;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.NameType;
import tc.oc.commons.core.scheduler.Scheduler;

/**
 * Manages the Bukkit aspects of a player's name and appearance
 */
@Singleton
public class PlayerAppearanceChanger {

    private static final NameType REAL_NAME_TYPE = new NameType(NameStyle.VERBOSE, true, true, false, false, false);
    private static final NameType NICKNAME_TYPE = new NameType(NameStyle.VERBOSE, true, false, false, false, false);

    private final IdentityProvider identityProvider;
    private final NicknameConfiguration config;
    private final Scheduler scheduler;
    private final FullNameRenderer nameRenderer;
    private final UsernameRenderer usernameRenderer;
    private final FlairRenderer flairRenderer;

    @Inject
    PlayerAppearanceChanger(IdentityProvider identityProvider, NicknameConfiguration config, Scheduler scheduler, FullNameRenderer nameRenderer, UsernameRenderer usernameRenderer, FlairRenderer flairRenderer) {
        this.identityProvider = identityProvider;
        this.config = config;
        this.scheduler = scheduler;
        this.nameRenderer = nameRenderer;
        this.usernameRenderer = usernameRenderer;
        this.flairRenderer = flairRenderer;
    }

    /**
     * Refresh the given player's appearance for all viewers.
     */
    public void refreshPlayer(Player player) {
        refreshPlayer(player, identityProvider.currentIdentity(player));
    }

    /**
     * Refresh the given player's appearance for all viewers, assuming the given identity is their current one.
     *
     * This is necessary if the player's identity changes, or if their current identity's name is invalidated.
     */
    public void refreshPlayer(final Player player, final Identity identity) {
        player.setDisplayName(nameRenderer.getLegacyName(identity, REAL_NAME_TYPE));

        final String legacyNickname = renderLegacyNickname(identity);
        for(Player viewer : player.getServer().getOnlinePlayers()) {
            refreshFakeNameAndSkin(player, identity, legacyNickname, viewer);
        }

        if(config.overheadFlair()) {
            String prefix = usernameRenderer.getColor(identity, REAL_NAME_TYPE).toString();
            if(identity.getNickname() == null) {
                prefix = flairRenderer.getLegacyName(identity, REAL_NAME_TYPE) + prefix;
            }
            setOverheadNamePrefix(player, prefix);
        }
    }

    /**
     * Refresh the appearance of all players for the given viewer
     */
    public void refreshViewer(final Player viewer) {
        for(Player player : viewer.getServer().getOnlinePlayers()) {
            final Identity identity = identityProvider.currentIdentity(player);
            refreshFakeNameAndSkin(player, identity, renderLegacyNickname(identity), viewer);
        }
    }

    /**
     * Release any resources being used to maintain the given player's appearance
     */
    public void cleanupAfterPlayer(Player player) {
        if(config.overheadFlair()) {
            // Remove players from their "overhead flair team" on quit
            final Team team = player.getServer().getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
            if(team != null) {
                scheduler.debounceTask(() -> team.removePlayer(player));
            }
        }
    }

    /**
     * Refresh the given player's fake appearance for the given viewer, assuming the given identity
     */
    private void refreshFakeNameAndSkin(Player player, Identity identity, @Nullable String fakeDisplayName, Player viewer) {
        if(identity.isRevealed(viewer)) {
            player.setFakeNameAndSkin(viewer, null, null);
            player.setFakeDisplayName(viewer, null);
        } else {
            player.setFakeNameAndSkin(viewer, identity.getNickname(), Skin.EMPTY);
            player.setFakeDisplayName(viewer, fakeDisplayName);
        }
    }

    /**
     * Sets a prefix for a player's overhead name by adding them to a scoreboard team.
     * Don't use this if scoreboard teams are being used for any other purpose.
     */
    private static void setOverheadNamePrefix(Player player, String prefix) {
        final Scoreboard scoreboard = player.getServer().getScoreboardManager().getMainScoreboard();
        prefix = prefix.substring(0, Math.min(prefix.length(), 14));

        Team team = scoreboard.getTeam(prefix);
        if(team == null) {
            team = scoreboard.registerNewTeam(prefix);
            team.setPrefix(prefix);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        team.addPlayer(player);
    }

    private @Nullable String renderLegacyNickname(Identity identity) {
        return identity.getNickname() == null ? null : nameRenderer.getLegacyName(identity, NICKNAME_TYPE);
    }
}
