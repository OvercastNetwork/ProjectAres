package net.anxuiz.tourney.vote;

import java.util.Collection;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import net.anxuiz.tourney.MapClassification;
import tc.oc.api.docs.Entrant;
import tc.oc.pgm.map.PGMMap;

public interface VetoVote {

    Collection<Entrant> getCurrentTurnRemainingTeams();

    ImmutableSet<MapClassification> getRemainingClassifications();

    ImmutableSet<PGMMap> getRemainingMaps();

    Collection<Entrant> getParticipatingTeams();

    /**
     * Gets the communally-selected {@link MapClassification}, or
     * <code>null</code> if none has been determined yet.
     *
     * @return The communally-selected {@link MapClassification}, or
     * <code>null</code> if none has been determined yet.
     */
    @Nullable
    MapClassification getSelectedClassification();

    /**
     * Gets the communally-selected {@link PGMMap}, or <code>null</code> if none has been determined yet.
     *
     * @return The communally-selected {@link PGMMap}, or <code>null</code> if none has been determined yet.
     */
    @Nullable
    PGMMap getSelectedMap();

    void registerVeto(Entrant entrant, PGMMap map);

    void registerVeto(Entrant entrant, MapClassification classification);

    /** Cycles the voting turn. */
    void cycleTurn();
}
