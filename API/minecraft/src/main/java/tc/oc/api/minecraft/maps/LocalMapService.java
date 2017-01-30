package tc.oc.api.minecraft.maps;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.MapRating;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.maps.MapRatingsRequest;
import tc.oc.api.maps.MapRatingsResponse;
import tc.oc.api.maps.MapService;
import tc.oc.api.maps.MapUpdateMultiResponse;
import tc.oc.api.minecraft.users.UserStore;
import tc.oc.api.model.NullModelService;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.minecraft.api.entity.Player;

@Singleton
public class LocalMapService extends NullModelService<MapDoc, MapDoc> implements MapService {

    @Inject private UserStore<Player> userStore;

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
        return Futures.immediateFuture(new MapUpdateMultiResponse(
            maps.stream()
                .flatMap(map -> Stream.concat(map.author_uuids().stream(),
                                              map.contributor_uuids().stream()))
                .collect(Collectors.mappingTo(uuid -> userStore.byUuid(uuid)
                                                               .flatMap(userStore::user)
                                                               .orElse(null)))
        ));
    }
}
