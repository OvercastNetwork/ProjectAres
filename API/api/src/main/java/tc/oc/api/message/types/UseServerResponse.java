package tc.oc.api.message.types;

import javax.annotation.Nullable;
import tc.oc.api.annotations.Serialize;

@Serialize
public interface UseServerResponse extends Reply {
    String server_name();
    boolean now();

    UseServerResponse EMPTY = new UseServerResponse() {
        @Override public String server_name() {
            return "default";
        }

        @Override public boolean success() {
            return true;
        }

        @Nullable @Override public String error() {
            return null;
        }

        @Override public boolean now() {
            return false;
        }
    };
}
