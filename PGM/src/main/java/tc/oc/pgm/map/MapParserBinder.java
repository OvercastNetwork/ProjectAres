package tc.oc.pgm.map;

import com.google.inject.Binder;
import tc.oc.commons.core.inject.SetBinder;

public class MapParserBinder extends SetBinder<MapRootParser> {
    public MapParserBinder(Binder binder) {
        super(binder);
    }
}
