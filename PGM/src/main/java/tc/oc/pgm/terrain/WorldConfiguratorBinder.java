package tc.oc.pgm.terrain;

import com.google.inject.Binder;
import tc.oc.commons.core.inject.SetBinder;

public class WorldConfiguratorBinder extends SetBinder<WorldConfigurator> {
    public WorldConfiguratorBinder(Binder binder) {
        super(binder);
    }
}
