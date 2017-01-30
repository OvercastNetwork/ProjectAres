package tc.oc.api.whispers;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Whisper;
import tc.oc.api.docs.virtual.WhisperDoc;
import tc.oc.api.model.ModelService;

public interface WhisperService extends ModelService<Whisper, WhisperDoc.Partial> {

    ListenableFuture<Whisper> forReply(PlayerId user);
}
