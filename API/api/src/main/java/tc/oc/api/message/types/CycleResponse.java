package tc.oc.api.message.types;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;

@Serialize
public interface CycleResponse extends Reply {

    // Player UUID -> server_id
    // null server_id is lobby
    Map<UUID, String> destinations();

    CycleResponse EMPTY = new CycleResponse() {
        @Override
        public Map<UUID, String> destinations() {
            return Collections.emptyMap();
        }

        @Override
        public boolean success() {
            return true;
        }

        @Override
        public @Nullable String error() {
            return null;
        }
    };
}
