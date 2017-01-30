package tc.oc.pgm.goals;

import javax.annotation.Nullable;

import org.bukkit.DyeColor;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.teams.TeamMatchModule;

/**
 * A goal with an owning team. Match-time companion to {@link OwnedGoal}
 */
public abstract class OwnedGoal<T extends OwnableGoalDefinition> extends SimpleGoal<T> {

    protected final Team owner;

    public OwnedGoal(T definition, Match match) {
        super(definition, match);
        this.owner = definition.getOwner() == null ? null : match.needMatchModule(TeamMatchModule.class).team(definition.getOwner());
    }

    public @Nullable Team getOwner() {
        return this.owner;
    }

    @Override
    public DyeColor getDyeColor() {
        return owner != null ? BukkitUtils.chatColorToDyeColor(owner.getColor())
                             : DyeColor.WHITE;
    }

    @Override
    public abstract MatchDoc.OwnedGoal getDocument();

    class Document extends SimpleGoal.Document implements MatchDoc.OwnedGoal {
        @Override
        public @Nullable String owner_id() {
            return getOwner() == null ? null : getOwner().slug();
        }

        @Override
        public @Nullable String owner_name() {
            return getOwner() == null ? null : getOwner().getName();
        }
    }
}
