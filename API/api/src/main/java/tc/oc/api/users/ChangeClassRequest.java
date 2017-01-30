package tc.oc.api.users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface ChangeClassRequest extends Document {
    @Nonnull String category();
    @Nullable String name();
}
