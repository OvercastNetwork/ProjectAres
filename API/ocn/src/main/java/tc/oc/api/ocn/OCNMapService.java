package tc.oc.api.ocn;

import java.util.Collection;
import javax.inject.Singleton;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.MapRating;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.http.HttpOption;
import tc.oc.api.maps.MapRatingsRequest;
import tc.oc.api.maps.MapRatingsResponse;
import tc.oc.api.maps.MapService;
import tc.oc.api.maps.MapUpdateMultiResponse;
import tc.oc.api.model.HttpModelService;

@Singleton
class OCNMapService extends HttpModelService<MapDoc, MapDoc> implements MapService {

    public ListenableFuture<Object> rate(MapRating rating) {
        return this.client().post(memberUri(rating.map_id, "rate"), rating, Object.class, HttpOption.INFINITE_RETRY);
    }

    public ListenableFuture<MapRatingsResponse> getRatings(MapRatingsRequest request) {
        return this.client().post(memberUri(request.map_id, "get_ratings"), request, MapRatingsResponse.class, HttpOption.INFINITE_RETRY);
    }

    public ListenableFuture<MapUpdateMultiResponse> updateMapsAndLookupAuthors(Collection<? extends MapDoc> maps) {
        return updateMulti(maps, MapUpdateMultiResponse.class);
    }
}
