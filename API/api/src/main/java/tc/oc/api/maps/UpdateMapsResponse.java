package tc.oc.api.maps;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.message.types.UpdateMultiResponse;

/**
 * Result of {@link MapService#updateMaps(Collection)}
 */
public class UpdateMapsResponse {

    private final ListenableFuture<UpdateMultiResponse> maps;
    private final Map<UUID, ListenableFuture<UserDoc.Identity>> authors;

    public UpdateMapsResponse(ListenableFuture<UpdateMultiResponse> maps, Map<UUID, ListenableFuture<UserDoc.Identity>> authors) {
        this.maps = maps;
        this.authors = authors;
    }

    /**
     * Result of updating all the maps
     */
    public ListenableFuture<UpdateMultiResponse> maps() {
        return maps;
    }

    /**
     * Result of each individual map contributor lookup,
     * which may not all arrive at the same time.
     */
    public Map<UUID, ListenableFuture<UserDoc.Identity>> authors() {
        return authors;
    }
}
