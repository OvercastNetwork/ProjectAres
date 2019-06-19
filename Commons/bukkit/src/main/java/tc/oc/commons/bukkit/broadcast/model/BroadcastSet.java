package tc.oc.commons.bukkit.broadcast.model;

import java.nio.file.Path;
import tc.oc.commons.core.inspect.Inspectable;

public class BroadcastSet extends Inspectable.Impl {

    @Inspect private final Path path;
    @Inspect private final BroadcastPrefix prefix;

    public BroadcastSet(Path path, BroadcastPrefix prefix) {
        this.path = path;
        this.prefix = prefix;
    }

    public Path path() {
        return path;
    }

    public BroadcastPrefix prefix() {
        return prefix;
    }
}
