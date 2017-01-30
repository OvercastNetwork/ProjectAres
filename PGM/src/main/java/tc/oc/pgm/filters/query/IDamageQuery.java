package tc.oc.pgm.filters.query;

import tc.oc.pgm.tracker.damage.DamageInfo;

public interface IDamageQuery extends IPlayerEventQuery {

    IPlayerQuery getVictim();

    DamageInfo getDamageInfo();
}
