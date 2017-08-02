package tc.oc.api.model;

import java.util.Collection;
import javax.inject.Inject;

import com.damnhandy.uri.template.UriTemplate;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.http.HttpClient;
import tc.oc.api.http.HttpOption;
import tc.oc.api.message.types.FindMultiRequest;
import tc.oc.api.message.types.FindMultiResponse;
import tc.oc.api.message.types.FindRequest;

import static com.google.common.base.Preconditions.checkArgument;

public class HttpQueryService<Complete extends Model> implements QueryService<Complete> {

    @Inject private ModelMeta<Complete, ?> meta;
    @Inject private HttpClient client;

    @Override
    public TypeToken<Complete> completeType() {
        return meta.completeType();
    }

    protected HttpClient client() {
        return this.client;
    }
    protected String collectionUri() {
        return '/' + meta.pluralName();
    }

    protected String collectionUri(String action) {
        return UriTemplate.fromTemplate("/{model}/{action}")
                          .set("model", meta.pluralName())
                          .set("action", action)
                          .expand();
    }

    protected String findMultiUri() {
        return collectionUri("find_multi");
    }

    protected String memberUri(String id) {
        return UriTemplate.fromTemplate("/{model}/{id}")
                          .set("model", meta.pluralName())
                          .set("id", id)
                          .expand();
    }

    protected String memberUri(String id, String action) {
        return UriTemplate.fromTemplate("/{model}/{id}/{action}")
                          .set("model", meta.pluralName())
                          .set("id", id)
                          .set("action", action)
                          .expand();
    }

    @Override
    public ListenableFuture<FindMultiResponse<Complete>> all() {
        return client().get(collectionUri(), meta.multiResponseType(), HttpOption.INFINITE_RETRY);
    }

    @Override
    public ListenableFuture<Complete> find(String id) {
        return client().get(memberUri(id), meta.completeType(), HttpOption.INFINITE_RETRY);
    }

    @Override
    public ListenableFuture<FindMultiResponse<Complete>> find(FindRequest<Complete> request) {
        return client().post(findMultiUri(), request, meta.multiResponseType(), HttpOption.INFINITE_RETRY);
    }

    @Override
    public ListenableFuture<FindMultiResponse<Complete>> find(Collection<String> ids) {
        return find(new FindMultiRequest(meta.partialTypeRaw(), ids));
    }
}
