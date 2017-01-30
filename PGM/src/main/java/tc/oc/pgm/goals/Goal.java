package tc.oc.pgm.goals;

import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.pgm.bossbar.BossBarSource;
import tc.oc.pgm.features.SluggedFeature;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.Party;

/**
 * TODO: Extract CompletableGoal which flags and CPs don't implement
 */
public interface Goal<T extends GoalDefinition> extends SluggedFeature<T> {

    /**
     * @return the {@link Match} this goal is part of
     */
    Match getMatch();

    /**
     * Test if the given team is allowed to complete this goal.
     */
    boolean canComplete(Competitor team);

    default Stream<Competitor> completers() {
        return getMatch().getCompetitors().stream().filter(this::canComplete);
    }

    /**
     * Test if this goal is completed by any team
     */
    boolean isCompleted();

    /**
     * Test if this goal is completed for the given team. If the goal's completion state is not
     * team-specific (e.g. a destroyable) then this will return true for any team that is allowed to
     * complete the goal, if it is complete.
     */
    boolean isCompleted(Competitor team);

    default boolean isCompleted(Optional<? extends Competitor> competitor) {
        return competitor.isPresent() ? isCompleted(competitor.get())
                                      : isCompleted();
    }

    /**
     * Returns true if this goal can be completed by multiple teams (e.g. a capture point).
     *
     * Currently, this affects how the goal is displayed on the scoreboard.
     */
    default boolean isShared() {
        return getDefinition().isShared();
    }

    /**
     * Returns true if the goal acts "normally".  Normal behavior is defined when the goal is visible
     * via mediums such as the {@link BossBarSource}, the Scoreboard, and chat.  If a call
     * to this method returns false, this goal will not show up anywhere.
     *
     * In most cases, this should simply delegate to {@link GoalDefinition#isVisible()}
     */
    boolean isVisible();

    boolean isRequired();

    /**
     * The name of the goal, as displayed to players on scoreboards and such. Usually delegates to
     * {@link GoalDefinition#getName()} but it's possible to implement a goal that can be renamed.
     */
    String getName();
    String getColoredName();
    BaseComponent getComponentName();

    /**
     * A color used for fireworks displays
     */
    Color getColor();
    DyeColor getDyeColor();

    ChatColor renderSidebarStatusColor(@Nullable Competitor competitor, Party viewer);

    String renderSidebarStatusText(@Nullable Competitor competitor, Party viewer);

    ChatColor renderSidebarLabelColor(@Nullable Competitor competitor, Party viewer);

    String renderSidebarLabelText(@Nullable Competitor competitor, Party viewer);

    MatchDoc.Goal getDocument();
}
