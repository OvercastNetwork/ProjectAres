package tc.oc.api.document;

import java.util.Map;

import tc.oc.api.docs.virtual.Document;

public interface DocumentGenerator {
    <T extends Document> T instantiate(DocumentMeta<T> meta, Document base, Map<String, Object> data);
}
