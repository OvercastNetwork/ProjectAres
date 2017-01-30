package tc.oc.pgm.stamina.mutators;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.pgm.utils.NumericModifier;

public abstract interface StaminaMutator {

    String getName();

    BaseComponent getDescription();

    NumericModifier getNumericModifier();
}
