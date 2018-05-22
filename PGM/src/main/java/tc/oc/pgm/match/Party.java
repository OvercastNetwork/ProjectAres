package tc.oc.pgm.match;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.bukkit.chat.Named;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.chat.MultiAudience;
import tc.oc.pgm.filters.Filterable;
import tc.oc.pgm.filters.query.IPartyQuery;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface Party extends Named, IPartyQuery, Filterable<IPartyQuery>, MultiAudience {

    enum Type { Participating, Observing }

    @Override
    default Party getParty() {
        return this;
    }

    /**
     * The name of this party at match load time (for privileged viewers). This cannot change at any time during the match.
     */
    String getDefaultName();

    /**
     * The current name of the party (for privileged viewers). May change at any time during the match.
     */
    default String getName() {
        return getDefaultName();
    }

    /**
     * The current name of the party. May change at any time during the match.
     */
    default String getName(@Nullable CommandSender viewer) {
        return getName();
    }

    /**
     * Should the name of the party be treated as a plural word grammatically?
     * This applies to all possible names returned from any of the name methods.
     */
    boolean isNamePlural();

    ChatColor getColor();

    default Color getFullColor() {
        return BukkitUtils.colorOf(getColor());
    }

    /**
     * The party's name (for privileged viewers) with color and no other decorations, legacy formatting
     */
    default String getColoredName() {
        return getColor() + getName();
    }

    /**
     * The party's name with color and no other decorations, legacy formatting
     */
    default String getColoredName(@Nullable CommandSender viewer) {
        return getColor() + getName(viewer);
    }

    /**
     * The party's name with color and no other decorations
     */
    default BaseComponent getComponentName() {
        return new Component(getName(), getColor());
    }

    @Override default BaseComponent getStyledName(NameStyle style) {
        return getComponentName();
    }

    /**
     * Everything before the player's name in chat output
     */
    default BaseComponent getChatPrefix() {
        return Components.blank();
    }

    /**
     * All players currently in this party
     */
    Set<MatchPlayer> getPlayers();

    default Stream<MatchPlayer> players() {
        return getPlayers().stream();
    }

    /**
     * Party member with the given ID, or null if they are not in this party
     */
    @Nullable MatchPlayer getPlayer(PlayerId playerId);

    default Optional<MatchPlayer> player(PlayerId playerId) {
        return players().filter(mp -> playerId.equals(mp.getPlayerId()))
                        .findAny();
    }

    /**
     * Called by {@link Match} to add a player to this party.
     *
     * This method should not be called from anywhere else. This method only
     * needs to modify the party's internal state. Everything else is
     * handled by {@link Match}.
     */
    boolean addPlayerInternal(MatchPlayer player);

    /**
     * Called by {@link Match} to remove a player from this party.
     *
     * This method should not be called from anywhere else. This method only
     * needs to modify the party's internal state. Everything else is
     * handled by {@link Match}.
     */
    boolean removePlayerInternal(MatchPlayer player);

    /**
     * If true, the party will be automatically added to the match when the first
     * player joins it, and automatically removed when it becomes empty. If false,
     * the party will never be added or removed by core PGM, and the providing
     * module must handle this.
     */
    default boolean isAutomatic() {
        return false;
    }

    Type getType();

    default boolean isParticipatingType() {
        return getType() == Type.Participating;
    }

    default boolean isParticipating() {
        return isParticipatingType() && getMatch().isRunning();
    }

    default boolean isObservingType() {
        return getType() == Type.Observing;
    }

    default boolean isObserving() {
        return isObservingType() || !getMatch().isRunning();
    }

    @Override
    default Optional<? extends Filterable<? super IPartyQuery>> filterableParent() {
        return Optional.of(getMatch());
    }

    @Override
    default Stream<? extends Filterable<? extends IPartyQuery>> filterableChildren() {
        return getPlayers().stream();
    }
}
