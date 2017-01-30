package tc.oc.pgm.goals;

import java.util.Optional;
import javax.annotation.Nullable;

import tc.oc.pgm.teams.TeamFactory;

/**
 * Definition of a goal that may be "owned" by a particular team. The ramifications of
 * ownership depend entirely on the type of goal. Some goals are pursued by their
 * owner, some are defended by their owner. The only thing the base class does with
 * the owner is store it and use it as part of the default ID.
 */
public interface OwnableGoalDefinition<G extends Goal<?>> extends GoalDefinition<G> {

    Optional<TeamFactory> owner();

    default @Nullable TeamFactory getOwner() {
        return owner().orElse(null);
    }
}
