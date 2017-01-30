package tc.oc.pgm.match;

import javax.annotation.Nullable;

import tc.oc.pgm.filters.query.IPlayerQuery;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents the competitive relationship between two player states. The attacker
 * can be null to indicate a neutral relationship i.e. "world" damage.
 *
 * Note a few subtleties:
 *  - SELF and ALLY are mutually exclusive.. a player is not their own ally
 *  - a player can be their own ENEMY if the two states have different parties
 */
public enum PlayerRelation {
    NEUTRAL, // attacker is null (e.g. world damage) or not participating
    SELF,    // same player, same team
    ALLY,    // different players, same team
    ENEMY;   // different teams (same/different player doesn't matter)

    public static PlayerRelation get(IPlayerQuery victim, @Nullable IPlayerQuery attacker) {
        checkNotNull(victim);

        if(attacker == null || !attacker.getParty().isParticipatingType()) {
            return NEUTRAL;
        } else if(!victim.getParty().equals(attacker.getParty())) {
            return ENEMY;
        } else if(victim.equals(attacker)) {
            return SELF;
        } else {
            return ALLY;
        }
    }

    public boolean are(IPlayerQuery victim, @Nullable IPlayerQuery attacker) {
        return this == get(victim, attacker);
    }
}
