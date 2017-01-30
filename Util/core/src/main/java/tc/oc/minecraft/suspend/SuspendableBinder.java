package tc.oc.minecraft.suspend;

import com.google.inject.Binder;
import tc.oc.commons.core.inject.SetBinder;

public class SuspendableBinder extends SetBinder<Suspendable> {
    public SuspendableBinder(Binder binder) {
        super(binder);
    }
}
