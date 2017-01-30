package tc.oc.api.maps;

import java.util.Collection;
import java.util.Collections;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.MapRating;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.model.NullModelService;

public class NullMapService extends NullModelService<MapDoc, MapDoc> implements MapService {

    @Override
    public ListenableFuture<?> rate(MapRating rating) {
        return Futures.immediateFuture(null);
    }

    @Override
    public ListenableFuture<MapRatingsResponse> getRatings(MapRatingsRequest request) {
        return Futures.immediateFuture(Collections::emptyMap);
    }

    @Override
    public ListenableFuture<MapUpdateMultiResponse> updateMapsAndLookupAuthors(Collection<? extends MapDoc> maps) {
        return Futures.immediateFuture(new MapUpdateMultiResponse(Collections.emptyMap()));
    }
}
