package net.anxuiz.tourney;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import tc.oc.pgm.map.PGMMap;

public class MapClassification {

    private final String name;
    private final Set<PGMMap> maps;

    public MapClassification(String name, Set<PGMMap> maps) {
        this.name = name;
        this.maps = ImmutableSet.copyOf(maps);
    }

    public String name() {
        return name;
    }

    public Set<PGMMap> maps() {
        return maps;
    }
}
