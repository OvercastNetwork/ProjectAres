package tc.oc.pgm.map;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.Ordering;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.commons.core.inject.MemberInjectingFactory;
import tc.oc.commons.core.util.Utils;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.modules.InfoModule;

/**
 * PGMMap is persistent through matches and represents an "anchor" that so that
 * map information can be reloaded easily.
 */
public class PGMMap extends MapDefinition {
    /**
     * Use a factory to create {@link PGMMap}s so that we can
     * bind PGMMap itself in {@link MapScoped}.
     */
    public static class Factory {
        private final MemberInjectingFactory<PGMMap> factory;

        @Inject Factory(MemberInjectingFactory<PGMMap> factory) {
            this.factory = factory;
        }

        public PGMMap create(MapFolder folder) {
            return factory.newInstance(folder);
        }
    }

    private boolean pushed;

    @Inject PGMMap(MapFolder folder) {
        super(folder);
    }

    public MapSource getSource() {
        return getFolder().getSource();
    }

    @Override
    public String getName() {
        return isLoaded() ? getInfo().name
                          : super.getName();
    }

    public MapInfo getInfo() {
        return getPersistentContext().getInfoModule().getMapInfo();
    }

    public MapId getId() {
        return getInfo().id;
    }

    public MapDoc getDocument() {
        return getPersistentContext().apiDocument();
    }

    @Override
    public String toString() {
        if(isLoaded()) {
            return getClass().getSimpleName() + "{id=" + getId() + " name=" + getName() + "}";
        } else {
            return getClass().getSimpleName() + "{" + getName() + " (not loaded)}";
        }
    }

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(PGMMap.class, this, obj, that ->
            this.getFolder().equals(that.getFolder())
        );
    }

    @Override
    public int hashCode() {
        return getFolder().hashCode();
    }

    @Override
    public boolean reload() throws MapNotFoundException {
        if(super.reload()) {
            this.pushed = false;
            return true;
        }
        return false;
    }

    public boolean isPushed() {
        return pushed;
    }

    public void markPushed() {
        pushed = true;
    }

    public static class DisplayOrder extends Ordering<PGMMap> {
        @Override
        public int compare(@Nullable PGMMap left, @Nullable PGMMap right) {
            return Ordering.natural().nullsLast().compare(
                left == null ? null : left.getInfo(),
                right == null ? null : right.getInfo()
            );
        }
    }
}
