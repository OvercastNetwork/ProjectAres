package tc.oc.api.maps;

import com.google.inject.multibindings.OptionalBinder;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class MapModelManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        bindModel(MapDoc.class, model -> {
            model.bindService().to(MapService.class);
        });

        OptionalBinder.newOptionalBinder(publicBinder(), MapService.class);
    }
}
