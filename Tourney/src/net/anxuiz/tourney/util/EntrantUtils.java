package net.anxuiz.tourney.util;

import javax.inject.Inject;

import com.google.common.base.Preconditions;
import net.anxuiz.tourney.Config;
import net.anxuiz.tourney.MapClassification;
import tc.oc.api.docs.Entrant;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.pgm.map.MapLibrary;
import tc.oc.pgm.map.PGMMap;

public class EntrantUtils {

    private final MapLibrary mapLibrary;

    @Inject EntrantUtils(MapLibrary mapLibrary) {
        this.mapLibrary = mapLibrary;
    }

    public int getClassificationPlayCount(Entrant entrant, MapClassification classification) {
        Preconditions.checkNotNull(classification, "Classification");
        int count = 0;
        for (MatchDoc match : Preconditions.checkNotNull(entrant, "Entrant").matches()) {
            if (Config.classificationMatchFamilies().contains(match.family_id())) {
                final PGMMap map = mapLibrary.getMapById(match.map()._id());
                if(map != null && classification.maps().contains(map)) {
                    count++;
                }
            }
        }

        return count;
    }

    public int getMapPlayCount(Entrant entrant, PGMMap map) {
        Preconditions.checkNotNull(map, "Map");
        int count = 0;
        for (MatchDoc match : Preconditions.checkNotNull(entrant, "Entrant").matches()) {
            if (Config.mapMatchFamilies().contains(match.family_id())) {
                if(map.getDocument().equals(match.map())) {
                    count++;
                }
            }
        }

        return count;
    }
}
