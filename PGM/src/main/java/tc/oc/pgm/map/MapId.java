package tc.oc.pgm.map;

import java.util.Objects;

import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.commons.core.formatting.StringUtils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class MapId {
    public static final String SLUG_PATTERN = "^[a-z0-9_]+$";

    private final String slug;
    private final MapDoc.Edition edition;
    private final MapDoc.Phase phase;

    public MapId(String slug, MapDoc.Edition edition, MapDoc.Phase phase) {
        this.slug = checkNotNull(slug);
        this.edition = checkNotNull(edition);
        this.phase = checkNotNull(phase);

        checkArgument(slug.matches(SLUG_PATTERN), "Invalid map slug \"" + slug + '"');
    }

    /**
     * Note: it's important that the input is not changed if it is already a valid slug
     */
    public static String slugifyName(String name) {
        return StringUtils.slugify(name, '_');
    }

    /**
     * Parse a {@link MapId} from the given string.
     *
     * If the input is a valid "slug:edition:phase" format, it is parsed as such,
     * otherwise the entire input is treated as a map name and slugified to create
     * a MapId with the default edition and phase.
     */
    public static MapId parse(String text) {
        String[] parts = text.split(":");

        if(parts.length == 3 && parts[0].equals(slugifyName(parts[0]))) {
            try {
                final MapDoc.Edition edition = MapDoc.Edition.valueOf(parts[1].toUpperCase());
                final MapDoc.Phase phase = MapDoc.Phase.valueOf(parts[2].toUpperCase());
                return new MapId(parts[0], edition, phase);
            } catch(IllegalArgumentException ignored) {}
        }

        return new MapId(slugifyName(text),
                         MapDoc.Edition.STANDARD,
                         MapDoc.Phase.PRODUCTION);
    }

    public String slug() {
        return slug;
    }

    public MapDoc.Edition edition() {
        return edition;
    }

    public MapDoc.Phase phase() {
        return phase;
    }

    public boolean isDefault() {
        return edition == MapDoc.Edition.DEFAULT &&
               phase == MapDoc.Phase.DEFAULT;
    }

    @Override
    public String toString() {
        if(isDefault()) {
            return slug;
        } else {
            return slug + ':' + edition.name().toLowerCase() + ':' + phase.name().toLowerCase();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof MapId)) return false;
        MapId id = (MapId) o;
        return phase == id.phase &&
               Objects.equals(slug, id.slug) &&
               edition == id.edition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slug, edition, phase);
    }
}
