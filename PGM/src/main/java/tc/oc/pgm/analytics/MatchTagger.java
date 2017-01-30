package tc.oc.pgm.analytics;

import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import tc.oc.analytics.Tag;
import tc.oc.analytics.Tagger;
import tc.oc.pgm.map.MapId;
import tc.oc.pgm.match.Match;

public class MatchTagger implements Tagger {

    private final ImmutableSet<Tag> tags;

    @Inject MatchTagger(MapId mapId, Match match) {
        tags = ImmutableSet.of(
            Tag.of("match_id", match.getId()),
            Tag.of("map", mapId.slug()),
            Tag.of("map_edition", mapId.edition().toString().toLowerCase()),
            Tag.of("map_phase", mapId.phase().toString().toLowerCase())
        );
    }

    @Override
    public ImmutableSet<Tag> tags() {
        return tags;
    }
}
