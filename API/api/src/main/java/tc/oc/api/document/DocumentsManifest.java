package tc.oc.api.document;

import tc.oc.api.docs.virtual.Document;
import tc.oc.api.serialization.GsonBinder;
import tc.oc.commons.core.inject.HybridManifest;

public class DocumentsManifest extends HybridManifest {

    @Override
    protected void configure() {
        bindAndExpose(DocumentSerializer.class);
        bind(DocumentRegistry.class);
        bind(DocumentGenerator.class).to(ProxyDocumentGenerator.class);

        new GsonBinder(publicBinder())
            .bindHiearchySerializer(Document.class)
            .to(DocumentSerializer.class);
    }
}
