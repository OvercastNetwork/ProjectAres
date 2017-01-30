package tc.oc.document;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface InterfaceDoc extends Document {
    int number();
    String text();
}
