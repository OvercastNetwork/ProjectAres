package tc.oc.api.users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface ChangeSettingRequest extends Document {
    @Nonnull String profile();
    @Nonnull String setting();
    @Nullable String value();
}
