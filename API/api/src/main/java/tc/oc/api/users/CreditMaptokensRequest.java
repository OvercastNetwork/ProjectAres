package tc.oc.api.users;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface CreditMaptokensRequest extends Document {
    int maptokens();
}
