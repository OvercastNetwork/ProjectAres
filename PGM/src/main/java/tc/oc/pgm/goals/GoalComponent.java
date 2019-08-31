package tc.oc.pgm.goals;

import java.util.Objects;
import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.chat.RenderableComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.ImmutableComponent;
import tc.oc.commons.core.util.Utils;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Displays the name of a {@link Goal}, with optional status icon, as they appear
 * in the sidebar.
 */
public class GoalComponent extends ImmutableComponent implements RenderableComponent {

    private final Goal<?> goal;
    private final @Nullable Competitor competitor;
    private final boolean showStatus;

    public GoalComponent(Goal<?> goal, @Nullable Competitor competitor, boolean showStatus) {
        this.goal = goal;
        this.competitor = competitor;
        this.showStatus = showStatus;
    }

    /**
     * Display the status of a goal with respect to a particular {@link Competitor}.
     *
     * The goal will appear is it does on the sidebar when grouped under that competitor.
     */
    public static GoalComponent forCompetitor(Goal<?> goal, Competitor competitor, boolean showStatus) {
        return new GoalComponent(goal, checkNotNull(competitor), showStatus);
    }

    /**
     * Display the status of a goal in a generic way.
     *
     * The goal will appear as it does on the top of the sidebar, when not grouped under any competitor.
     */
    public static GoalComponent forEveryone(Goal<?> goal, boolean showStatus) {
        return new GoalComponent(goal, null, showStatus);
    }

    public Goal<?> goal() {
        return goal;
    }

    @Nullable
    public Competitor competitor() {
        return competitor;
    }

    public boolean showStatus() {
        return showStatus;
    }

    @Override
    public GoalComponent duplicate() {
        return new GoalComponent(goal, competitor, showStatus);
    }

    @Override
    public BaseComponent duplicateWithoutFormatting() {
        return duplicate();
    }

    @Override
    public BaseComponent render(ComponentRenderContext context, CommandSender viewer) {
        final Match match = goal.getMatch();
        final MatchPlayer player = match.getPlayer(viewer);
        final Party party = player != null ? player.getParty()
                                           : match.getDefaultParty();

        final Component c = new Component(goal.renderSidebarLabelColor(competitor, party));
        if(showStatus) {
            c.extra(new Component(goal.renderSidebarStatusText(competitor, party),
                                  goal.renderSidebarStatusColor(competitor, party)))
             .extra(" ");
        }
        c.extra(goal.renderSidebarLabelText(competitor, party));
        return c;
    }

    @Override
    protected boolean equals(BaseComponent obj) {
        return Utils.equals(GoalComponent.class, this, obj, that ->
            goal.equals(that.goal()) &&
            Objects.equals(competitor, that.competitor()) &&
            showStatus == that.showStatus() &&
            super.equals(that)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), goal, competitor, showStatus);
    }
}
