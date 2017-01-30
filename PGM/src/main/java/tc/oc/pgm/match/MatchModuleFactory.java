package tc.oc.pgm.match;

import javax.annotation.Nullable;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.TypeParameterCache;
import tc.oc.pgm.module.ModuleLoadException;

/**
 * Creates a {@link MatchModule} to be loaded into a {@link Match},
 * or returns null to indicate that the module is not needed.
 */
@Deprecated
public interface MatchModuleFactory<T extends MatchModule> {

    TypeParameterCache<MatchModuleFactory, MatchModule> MATCH_MODULE_TYPE_CACHE = new TypeParameterCache<>(MatchModuleFactory.class, "T");

    static <T extends MatchModule> TypeLiteral<T> matchModuleType(TypeLiteral<? extends MatchModuleFactory<T>> factoryType) {
        return (TypeLiteral<T>) MATCH_MODULE_TYPE_CACHE.resolve(factoryType);
    }

    /**
     * Return a new {@link T} module for the given {@link Match},
     * or null to omit the module for that match.
     *
     * If a module is returned, it's members are injected automatically,
     * which means this method must NOT inject anything into the module
     * itself.
     */
    @Nullable T createMatchModule(Match match) throws ModuleLoadException;
}
