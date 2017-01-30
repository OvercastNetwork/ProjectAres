package tc.oc.api.docs;

import java.time.Instant;
import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.DeletableModel;

public class BasicDeletableModel extends BasicModel implements DeletableModel {

    @Serialize private Instant died_at;

    @Override public @Nullable Instant died_at() {
        return died_at;
    }

    @Override
    public boolean dead() {
        return died_at() != null;
    }

    @Override
    public boolean alive() {
        return died_at() == null;
    }
}
