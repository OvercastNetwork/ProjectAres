package tc.oc.pgm.goals;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.pgm.features.FeatureFactory;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.features.SluggedFeatureDefinition;
import tc.oc.pgm.match.Match;

/**
 * Definition of a goal/objective feature. Provides a name field, used to identify
 * the goal to players, and to generate a default ID. There is also a visibility
 * flag. An invisible goal does not appear in any scoreboards, chat messages, or
 * anything else that would directly indicate its existence.
 */
@FeatureInfo(name = "objective")
public interface GoalDefinition<G extends Goal<?>> extends SluggedFeatureDefinition, FeatureFactory<G> {

    String getName();

    String getColoredName();

    BaseComponent getComponentName();

    boolean isShared();

    @Nullable Boolean isRequired();

    boolean isVisible();

    default G getGoal(Match match) {
        return match.features().get(this);
    }
}
