package tc.oc.api.docs;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.docs.virtual.Model;

@Serialize
public interface Tournament extends Model {

    String name();
    Instant start();
    Instant end();

    int min_players_per_match();
    int max_players_per_match();

    List<team.Id> accepted_teams();

    default List<String> acceptedTeamNames() {
        return Lists.transform(accepted_teams(), team.Id::name);
    }

    /**
     * game type -> [map ids]
     *
     * Key is some kind of string identifying game type e.g. "core", "wool", "tdm"
     *
     * Value is a set of {@link MapDoc#_id()}
     */
    List<MapClassification> map_classifications();

    @Serialize
    interface MapClassification extends Document {
        String name();
        Set<String> map_ids();
    }
}
