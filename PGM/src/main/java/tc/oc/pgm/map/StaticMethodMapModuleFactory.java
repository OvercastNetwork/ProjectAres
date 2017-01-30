package tc.oc.pgm.map;

import com.google.inject.ProvisionException;
import tc.oc.commons.core.reflect.Delegates;
import tc.oc.commons.core.reflect.ResolvableType;

/**
 * Delegates module parsing to a static method on the module class,
 * which must match the name and signature of {@link MapModuleParser#parse}.
 */
public abstract class StaticMethodMapModuleFactory<T extends MapModule> extends MapModuleManifest<T> {
    @Override
    protected MapModuleParser<T> parser() {
        try {
            return Delegates.newStaticMethodDelegate(
                new ResolvableType<MapModuleParser<T>>(){}.in(getClass()),
                rawType
            );
        } catch(NoSuchMethodError e) {
            throw new ProvisionException(e.getMessage(), e);
        }
    }
}
