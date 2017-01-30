package tc.oc.pgm.stamina.mutators;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.pgm.utils.NumericModifier;

public class SimpleMutator implements StaminaMutator {

    private final String name;
    private final NumericModifier numericModifier;
    private final BaseComponent description;

    public SimpleMutator(String name, NumericModifier modifier, BaseComponent description) {
        this.name = name;
        numericModifier = modifier;
        this.description = description;
    }

    public SimpleMutator(String name, NumericModifier modifier, String descriptionKey) {
        this(name, modifier, new TranslatableComponent(descriptionKey));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BaseComponent getDescription() {
        return description;
    }

    @Override
    public NumericModifier getNumericModifier() {
        return numericModifier;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name=" + name + " mod=" + numericModifier + "}";
    }
}
