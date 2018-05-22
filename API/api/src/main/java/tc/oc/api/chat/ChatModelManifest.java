package tc.oc.api.chat;

import tc.oc.api.docs.Chat;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class ChatModelManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        bindModel(Chat.class, ChatDoc.Partial.class, model -> {
            model.bindDefaultService().to(model.nullService());
        });
    }
}
