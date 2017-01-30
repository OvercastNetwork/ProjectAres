package tc.oc.pgm.goals;

import java.util.Optional;

import tc.oc.pgm.teams.TeamFactory;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class OwnableGoalDefinitionImpl<G extends Goal<?>> extends GoalDefinitionImpl<G> implements OwnableGoalDefinition<G> {

    @Inspect(brief=true)
    private final Optional<TeamFactory> owner;

    public OwnableGoalDefinitionImpl(String name, @Nullable Boolean required, boolean visible, Optional<TeamFactory> owner) {
        super(name, required, visible);
        this.owner = checkNotNull(owner);
    }

    @Override
    public String defaultSlug() {
        final String slug = super.defaultSlug();
        return owner().map(team -> slug + "-" + team.defaultSlug())
                      .orElse(slug);
    }

    @Override
    public Optional<TeamFactory> owner() {
        return owner;
    }
}
