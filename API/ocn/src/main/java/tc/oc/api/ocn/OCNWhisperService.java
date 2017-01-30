package tc.oc.api.ocn;

import javax.inject.Singleton;

import com.damnhandy.uri.template.UriTemplate;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Whisper;
import tc.oc.api.docs.virtual.WhisperDoc;
import tc.oc.api.http.HttpOption;
import tc.oc.api.model.HttpModelService;
import tc.oc.api.whispers.WhisperService;

@Singleton
class OCNWhisperService extends HttpModelService<Whisper, WhisperDoc.Partial> implements WhisperService {

    @Override
    public ListenableFuture<Whisper> forReply(PlayerId user) {
        final String uri = UriTemplate.fromTemplate("/{model}/reply/{user}")
                                      .set("model", "whispers")
                                      .set("user", user._id())
                                      .expand();
        return client().get(uri, Whisper.class, HttpOption.INFINITE_RETRY);
    }
}
