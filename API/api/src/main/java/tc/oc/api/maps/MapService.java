package tc.oc.api.maps;

import java.util.Collection;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.MapRating;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.model.ModelService;

public interface MapService extends ModelService<MapDoc, MapDoc> {

    ListenableFuture<?> rate(MapRating rating);

    ListenableFuture<MapRatingsResponse> getRatings(MapRatingsRequest request);

    /**
     * Send map updates to the backend, and retrieve data about map contributors.
     */
    UpdateMapsResponse updateMaps(Collection<? extends MapDoc> maps);
}
