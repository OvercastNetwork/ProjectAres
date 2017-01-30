package tc.oc.api.maps;

import java.util.Map;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface MapRatingsResponse extends Document {
    Map<UserId, Integer> player_ratings();
}
