package tc.oc.pgm.mutation.submodule;

import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.Match;

/**
 * A mutation modules that injects special kits on participant spawn.
 */
public class KitMutationModule extends MutationModule {

    protected final Kit[] kits;
    protected final boolean force;

    public KitMutationModule(Match match, boolean force, Kit... kits) {
        super(match);
        this.kits = kits;
        this.force = force;
    }

    public Kit[] getKits() {
        return enabled ? kits : new Kit[0];
    }

    public boolean isForceful() {
        return force;
    }

}
