package tc.oc.pgm.goals;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.features.FeatureDefinition;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class GoalDefinitionImpl<G extends Goal<?>> extends FeatureDefinition.Impl implements GoalDefinition<G> {
    private final @Inspect @Nullable Boolean required;
    private final @Inspect boolean visible;
    private final @Inspect String name;

    public GoalDefinitionImpl(String name, @Nullable Boolean required, boolean visible) {
        this.name = checkNotNull(name);
        this.required = required;
        this.visible = visible;
    }

    @Override
    public String defaultSlug() {
        return GoalDefinition.super.defaultSlug() + "-" + slugify(getName());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getColoredName() {
        return this.getName();
    }

    @Override
    public BaseComponent getComponentName() {
        return new Component(getName());
    }

    @Override
    public @Nullable Boolean isRequired() {
        return this.required;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }
}
