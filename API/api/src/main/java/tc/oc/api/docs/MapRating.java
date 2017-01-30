package tc.oc.api.docs;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.MapDoc;

import javax.annotation.Nullable;

@Serialize
public class MapRating {
    public final String player_id;
    public final @Nullable String map_id;
    public final String map_name;
    public final String map_version;
    public final int score;
    public final String comment;

    public MapRating(UserId player, MapDoc map, int score, String comment) {
        this.player_id = player.player_id();
        this.map_id = map._id();
        this.map_name = map.name();
        this.map_version = map.version().toString();
        this.score = score;
        this.comment = comment;
    }
}
