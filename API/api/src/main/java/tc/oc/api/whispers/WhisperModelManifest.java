package tc.oc.api.whispers;

import com.google.inject.multibindings.OptionalBinder;
import tc.oc.api.docs.Whisper;
import tc.oc.api.docs.virtual.WhisperDoc;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class WhisperModelManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        bindModel(Whisper.class, WhisperDoc.Partial.class, model -> {
            model.bindService().to(WhisperService.class);
        });

        OptionalBinder.newOptionalBinder(publicBinder(), WhisperService.class)
                      .setDefault().to(NullWhisperService.class);

    }
}
