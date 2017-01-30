package tc.oc.api.users;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface CreditRaindropsRequest extends Document {
    int raindrops();
}
