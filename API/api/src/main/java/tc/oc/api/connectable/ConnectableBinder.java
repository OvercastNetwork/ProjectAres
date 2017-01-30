package tc.oc.api.connectable;

import com.google.inject.Binder;
import tc.oc.commons.core.inject.SetBinder;

public class ConnectableBinder extends SetBinder<Connectable> {
    public ConnectableBinder(Binder binder) {
        super(binder);
    }
}
