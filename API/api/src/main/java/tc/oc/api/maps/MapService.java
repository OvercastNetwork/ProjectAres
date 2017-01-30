package tc.oc.api.maps;

import java.util.Collection;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.MapRating;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.model.ModelService;

public interface MapService extends ModelService<MapDoc, MapDoc> {

    ListenableFuture<?> rate(MapRating rating);

    ListenableFuture<MapRatingsResponse> getRatings(MapRatingsRequest request);

    ListenableFuture<MapUpdateMultiResponse> updateMapsAndLookupAuthors(Collection<? extends MapDoc> maps);
}
