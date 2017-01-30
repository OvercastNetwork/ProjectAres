package tc.oc.api.maps;

import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.MapDoc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MapRatingsRequest {
    public final @Nullable String map_id;
    public final String map_name;
    public final String map_version; // TODO: use SemanticVersion class
    public final List<String> player_ids;

    public MapRatingsRequest(MapDoc map, Collection<? extends UserId> userIds) {
        this.map_id = map._id();
        this.map_name = map.name();
        this.map_version = map.version().toString();

        this.player_ids = new ArrayList<>(userIds.size());
        for(UserId userId : userIds) this.player_ids.add(userId.player_id());
    }
}
