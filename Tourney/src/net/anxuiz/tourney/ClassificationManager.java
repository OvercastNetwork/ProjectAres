package net.anxuiz.tourney;

import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import tc.oc.api.docs.Tournament;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.pgm.map.MapLibrary;
import tc.oc.pgm.map.PGMMap;

public class ClassificationManager {

    private final Set<MapClassification> classifications;

    @Inject ClassificationManager(Tournament tournament, MapLibrary mapLibrary) {
        this.classifications = tournament.map_classifications()
                                         .stream()
                                         .map(cl -> new MapClassification(cl.name(),
                                                                          cl.map_ids()
                                                                            .stream()
                                                                            .map(mapLibrary::getMapById)
                                                                            .collect(Collectors.toImmutableSet())))
                                         .collect(Collectors.toImmutableSet());
    }

    public @Nullable MapClassification firstClassificationForMap(PGMMap map) {
        for (MapClassification classification : this.classifications) {
            if (classification.maps().contains(map)) return classification;
        }

        return null;
    }

    public Set<MapClassification> classificationsForMap(final PGMMap map) {
        Preconditions.checkNotNull(map, "Map");
        return Sets.filter(this.classifications, classification -> classification.maps().contains(map));
    }

    public @Nullable MapClassification classificationFromSearch(String search) {
        return StringUtils.bestFuzzyMatch(Preconditions.checkNotNull(search, "Search"), this.classifications, 0.9);
    }

    public ImmutableSet<MapClassification> getClassifications() {
        return ImmutableSet.copyOf(this.classifications);
    }
}
