package tc.oc.pgm.ffa;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import tc.oc.api.bukkit.users.Users;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.virtual.CompetitorDoc;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.NullAudience;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;

/**
 * Wraps a single {@link MatchPlayer} in a free-for-all match.
 *
 * A Tribute is created on demand for a player the first time they join a match.
 * It is initially "empty", and the player has to be added to it, in the same way
 * they are added to any party.
 *
 * If the player leaves the match, they will be removed from the Tribute, and the
 * empty Tribute will be removed from the match. As with any inactive {@link Competitor},
 * the Tribute is retained by {@link Match}, indexed by {@link #getId}, which in this
 * case is the player's ID. If the player rejoins the same match, the FFA module will
 * retrieve their existing Tribute and add them back to it, instead of creating a new one.
 *
 * Attempting to add the wrong player, or add multiple players, will throw
 * {@link UnsupportedOperationException}.
 */
public class Tribute implements Competitor {

    public interface Factory {
        Tribute create(MatchPlayer player, @Nullable ChatColor color);
    }

    public static final ChatColor DEFAULT_COLOR = ChatColor.YELLOW;

    protected final IdentityProvider identityProvider;
    protected final ComponentRenderContext renderer;

    protected final Match match;
    protected final FreeForAllMatchModule ffa;
    protected final PlayerId playerId;
    protected final ChatColor color;

    protected @Nullable MatchPlayer player;
    protected Set<MatchPlayer> players = Collections.emptySet();

    @Inject Tribute(@Assisted MatchPlayer player, @Assisted @Nullable ChatColor color, IdentityProvider identityProvider, ComponentRenderContext renderer) {
        this.identityProvider = identityProvider;
        this.renderer = renderer;
        this.match = player.getMatch();
        this.ffa = match.needMatchModule(FreeForAllMatchModule.class);
        this.playerId = player.getPlayerId();
        this.color = color != null ? color : DEFAULT_COLOR;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{match=" + getMatch() + ", name=" + getName() + "}";
    }

    public PlayerId getPlayerId() {
        return playerId;
    }

    @Override
    public Match getMatch() {
        return match;
    }

    @Override
    public String getId() {
        return playerId.player_id();
    }

    @Override
    public String getDefaultName() {
        return getName();
    }

    @Override
    public String getName() {
        return playerId.username();
    }

    @Override
    public String getName(@Nullable CommandSender viewer) {
        return renderer.render(getComponentName(), viewer).toLegacyText();
    }

    @Override
    public boolean isNamePlural() {
        return false;
    }

    @Override
    public ChatColor getColor() {
        return color;
    }

    @Override
    public Color getFullColor() {
        return BukkitUtils.colorOf(getColor());
    }

    @Override
    public String getColoredName() {
        return getColor() + getName();
    }

    @Override
    public String getColoredName(@Nullable CommandSender viewer) {
        return getColor() + getName(viewer);
    }

    @Override
    public BaseComponent getComponentName() {
        return getStyledName(NameStyle.COLOR);
    }

    @Override
    public BaseComponent getStyledName(NameStyle style) {
        return new PlayerComponent(identityProvider.currentIdentity(playerId), style);
    }

    @Override
    public BaseComponent getChatPrefix() {
        return new Component();
    }

    @Override
    public Type getType() {
        return Type.Participating;
    }

    @Override
    public boolean isParticipatingType() {
        return true;
    }

    @Override
    public boolean isParticipating() {
        return getMatch().isRunning();
    }

    @Override
    public boolean isObservingType() {
        return false;
    }

    @Override
    public boolean isObserving() {
        return !getMatch().isRunning();
    }

    @Override
    public org.bukkit.scoreboard.Team.OptionStatus getNameTagVisibility() {
        return ffa.getOptions().nameTagVisibility;
    }

    @Override
    public Set<MatchPlayer> getPlayers() {
        return players;
    }

    @Override
    public Set<PlayerId> getPastPlayers() {
        return getMatch().isCommitted() ? Collections.singleton(playerId)
                                        : Collections.<PlayerId>emptySet();
    }

    @Override
    public void commit() {
    }

    @Override
    public @Nullable MatchPlayer getPlayer(PlayerId playerId) {
        return player != null && player.getPlayerId().equals(playerId) ? player : null;
    }

    protected void checkPlayer(MatchPlayer player) {
        if(!Users.equals(playerId, player.getBukkit())) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean addPlayerInternal(MatchPlayer player) {
        checkPlayer(player);
        boolean changed = this.player == null;
        this.player = player;
        this.players = Collections.singleton(player);
        return changed;
    }

    @Override
    public boolean removePlayerInternal(MatchPlayer player) {
        checkPlayer(player);
        boolean changed = this.player != null;
        this.player = null;
        this.players = Collections.emptySet();
        return changed;
    }

    @Override
    public boolean isAutomatic() {
        return true;
    }

    @Override
    public Stream<? extends Audience> audiences() {
        return player != null ? Stream.of(player) : Stream.empty();
    }

    @Override
    public CompetitorDoc getDocument() {
        return playerId;
    }
}
