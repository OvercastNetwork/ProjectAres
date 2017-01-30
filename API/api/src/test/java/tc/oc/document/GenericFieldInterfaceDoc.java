package tc.oc.document;

import java.util.List;

import java.time.Instant;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface GenericFieldInterfaceDoc extends Document {
    List<Instant> instants();
}
