package tc.oc.pgm.join;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.match.Competitor;

public interface JoinResult extends Comparable<JoinResult> {

    /**
     * Did the join succeed? If this is true, {@link #competitor()} should be present.
     */
    boolean isAllowed();

    /**
     * The {@link Competitor} that was joined.
     */
    default Optional<? extends Competitor> competitor() { return Optional.empty(); }

    /**
     * Message to display to the joining player (unformatted)
     */
    default Optional<BaseComponent> message() { return Optional.empty(); }

    /**
     * Display the message as an error
     */
    default boolean isError() { return false; }

    /**
     * The default result
     */
    default boolean isFallback() { return false; }

    /**
     * Extra messages to display after the primary one, fully formatted, each on a new line
     */
    default Collection<BaseComponent> extra() { return Collections.emptySet(); }

    /**
     * Include this join as an option in the UI (but disable it if the join didn't succeed)
     */
    default boolean isVisible() { return false; }

    /**
     * Player is rejoining a team they were previously on
     */
    default boolean isRejoin() { return false; }

    /**
     * Will another player be kicked as a result of this join?
     */
    default boolean priorityKickRequired() { return false; }

    /**
     * Lines of output that should be sent to the player
     */
    default Collection<BaseComponent> output() {
        final ImmutableList.Builder lines = ImmutableList.builder();
        if(message().isPresent()) {
            BaseComponent message = message().get();
            if(isError()) {
                message = new WarningComponent(message);
            } else {
                message = new Component(message, ChatColor.AQUA);
            }
            lines.add(message);
        }
        lines.addAll(extra());
        return lines.build();
    }

    @Override
    default int compareTo(JoinResult that) {
        return ComparisonChain.start()
            .compareFalseFirst(this.isFallback(), that.isFallback()) // Anything is better than this
            .compareFalseFirst(this.isAllowed(), that.isAllowed()) // Deny overrides allow
            .compareFalseFirst(this.priorityKickRequired(), that.priorityKickRequired()) // Avoid kick if possible
            .compareTrueFirst(this.isRejoin(), that.isRejoin()) // Rejoin is preferred
            .result();
    }
}
