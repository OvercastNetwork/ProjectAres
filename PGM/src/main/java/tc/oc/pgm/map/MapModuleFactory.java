package tc.oc.pgm.map;

/**
 * A {@link MapModuleManifest} that serves as its own {@link MapModuleParser}.
 */
public abstract class MapModuleFactory<M extends MapModule> extends MapModuleManifest<M> implements MapModuleParser<M> {
    @Override
    protected MapModuleParser<M> parser() {
        return this;
    }
}
