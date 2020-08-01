package tc.oc.api.whispers;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Whisper;
import tc.oc.api.docs.virtual.WhisperDoc;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.message.LocalMessageService;
import tc.oc.api.message.types.ModelUpdate;
import tc.oc.api.model.NullModelService;

public class LocalWhisperService extends NullModelService<Whisper, WhisperDoc.Partial> implements WhisperService {
    
    @Inject private LocalMessageService queue;
    
    @Override
    public ListenableFuture<Whisper> forReply(PlayerId user) {
        return Futures.immediateFailedFuture(new NotFound());
    }
    
    @Override
    public ListenableFuture<Whisper> update(WhisperDoc.Partial partial) {
        // Receives an Out object from WhisperDispatcher, and it implements Whisper, so we can just cast it.
        if (partial instanceof Whisper) {
            Whisper whisper = (Whisper) partial;
            queue.receive((ModelUpdate<Whisper>) () -> whisper, new TypeToken<ModelUpdate<Whisper>>(){});
            return Futures.immediateFuture(whisper);
        }
        return Futures.immediateFailedFuture(new NotFound());
    }
    
}
