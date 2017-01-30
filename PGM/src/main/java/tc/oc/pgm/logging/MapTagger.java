package tc.oc.pgm.logging;

import net.kencochrane.raven.event.EventBuilder;
import tc.oc.minecraft.logging.BetterRaven;
import tc.oc.pgm.map.MapDefinition;
import tc.oc.pgm.map.MapLogRecord;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.MatchManager;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

public class MapTagger implements BetterRaven.Helper {

    private final MatchManager matchManager;

    public MapTagger(MatchManager matchManager) {
        this.matchManager = matchManager;
    }

    @Override
    public void helpBuildingEvent(EventBuilder eventBuilder, LogRecord record) {
        if(record == null) return;

        // During match cycle, multiple matches (and maps) can be loaded,
        // and that is a popular time for errors to happen.
        Set<MapDefinition> maps = new HashSet<>();

        if(record instanceof MapLogRecord) {
            MapLogRecord mapRecord = (MapLogRecord) record;
            maps.add(mapRecord.getMap());
            eventBuilder.setCulprit(mapRecord.getLocation());
        }

        if(matchManager != null) {
            maps.addAll(matchManager.currentMatches().stream().map(Match::getMap).collect(Collectors.toList()));
        }

        int i = 0;
        for(MapDefinition map : maps) {
            String suffix = maps.size() == 1 ? "" : String.valueOf(i);
            eventBuilder.addTag("pgm_map_path" + suffix, map.getFolder().getAbsolutePath().toString());
            eventBuilder.addTag("pgm_map_name" + suffix, map.getName());
            if(map instanceof PGMMap && map.isLoaded()) {
                eventBuilder.addTag("pgm_map_version" + suffix, ((PGMMap) map).getInfo().version.toString());
            }
        }
    }
}
