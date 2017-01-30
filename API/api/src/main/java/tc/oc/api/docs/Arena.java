package tc.oc.api.docs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Model;

@Serialize
public interface Arena extends Model {
    @Nonnull String game_id();
    @Nonnull String datacenter();
    int num_playing();
    int num_queued();
    @Nullable String next_server_id();

    @Override @Serialize(false)
    default String toShortString() {
        return game_id() + "[" + datacenter() + "]";
    }
}
