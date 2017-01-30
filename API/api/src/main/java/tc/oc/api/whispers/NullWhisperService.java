package tc.oc.api.whispers;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Whisper;
import tc.oc.api.docs.virtual.WhisperDoc;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.model.NullModelService;

public class NullWhisperService extends NullModelService<Whisper, WhisperDoc.Partial> implements WhisperService {

    @Override
    public ListenableFuture<Whisper> forReply(PlayerId user) {
        return Futures.immediateFailedFuture(new NotFound());
    }
}
