package tc.oc.pgm.stamina;

import java.util.HashSet;
import java.util.Set;

import tc.oc.pgm.stamina.mutators.StaminaMutator;
import tc.oc.pgm.stamina.symptoms.StaminaSymptom;
import tc.oc.pgm.utils.NumericModifier;

public class StaminaOptions {

    public final Set<StaminaSymptom> symptoms = new HashSet<>();

    public static class Mutators {
        // cost per second
        public StaminaMutator stand;
        public StaminaMutator sneak;
        public StaminaMutator walk;
        public StaminaMutator run;

        // cost per action
        public StaminaMutator jump;
        public StaminaMutator runJump;
        public StaminaMutator meleeMiss;
        public StaminaMutator meleeHit;
        public StaminaMutator archery;

        // cost per health (half-heart)
        public StaminaMutator injury;
    }

    public final Mutators mutators = new Mutators();
}
