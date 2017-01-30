package tc.oc.api.model;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import tc.oc.api.docs.virtual.Document;
import tc.oc.api.docs.virtual.PartialModel;
import tc.oc.api.message.types.UpdateMultiRequest;

/**
 * Collects {@link Document}s to be sent to the API as a {@link UpdateMultiRequest}.
 * The documents are serialized immediately when added, which is useful if you want to
 * send the update at some later time when the document cannot be safely serialized.
 */
public class BatchUpdateRequest<T extends PartialModel> implements UpdateMultiRequest<JsonObject> {

    private final Gson gson;
    private final List<JsonObject> documents = new ArrayList<>();

    @Inject BatchUpdateRequest(Gson gson) {
        this.gson = gson;
    }

    @Override
    public List<JsonObject> documents() {
        return documents;
    }

    public void add(T document) {
        documents.add((JsonObject) gson.toJsonTree(document));
    }

    public void addAll(Iterable<? extends T> documents) {
        documents.forEach(this::add);
    }
}
