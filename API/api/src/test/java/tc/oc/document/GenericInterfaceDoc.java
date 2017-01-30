package tc.oc.document;

import java.util.List;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface GenericInterfaceDoc<T> extends Document {
    T value();
    List<T> values();
}
