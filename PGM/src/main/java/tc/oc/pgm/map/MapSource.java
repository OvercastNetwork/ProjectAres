package tc.oc.pgm.map;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import tc.oc.commons.core.util.Utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class MapSource implements Comparable<MapSource> {

    private final String key;
    private final @Nullable URL url;
    private final Path path;
    private final int maxDepth;
    private final Set<Path> onlyPaths;
    private final Set<Path> excludedPaths;
    private final int priority;        // Lowest priority source wins a map name conflict
    private final boolean globalIncludes;

    public MapSource(String key, Path path, @Nullable URL url, int maxDepth, Set<Path> onlyPaths, Set<Path> excludedPaths, int priority, boolean globalIncludes) {
        this.globalIncludes = globalIncludes;
        checkArgument(path.isAbsolute());
        this.key = key;
        this.url = url;
        this.path = checkNotNull(path);
        this.maxDepth = maxDepth;
        this.onlyPaths = checkNotNull(onlyPaths);
        this.excludedPaths = checkNotNull(excludedPaths);
        this.priority = priority;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "{key=" + key +
               " path=" + getPath().toString() +
               "}";
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(MapSource.class, this, obj, that -> this.key().equals(that.key));
    }

    @Override
    public int compareTo(MapSource o) {
        return Integer.compare(priority, o.priority);
    }

    public String key() {
        return key;
    }

    public boolean hasPriorityOver(MapSource o) {
        return compareTo(o) < 0;
    }

    public boolean globalIncludes() {
        return globalIncludes;
    }

    public Path getPath() {
        return path;
    }

    public @Nullable URL getUrl() {
        return url;
    }

    protected Set<Path> getRootPaths() {
        if(onlyPaths.isEmpty()) {
            return Collections.singleton(Paths.get(""));
        } else {
            return onlyPaths;
        }
    }

    protected boolean isExcluded(Path path) {
        path = getPath().relativize(path);
        for(Path excludedPath : excludedPaths) {
            if(path.startsWith(excludedPath)) return true;
        }
        return false;
    }

    public Set<Path> getMapFolders(final Logger logger) throws IOException {
        final Set<Path> mapFolders = new HashSet<>();
        for(Path root : getRootPaths()) {
            int depth = "".equals(root.toString()) ? 0 : Iterables.size(root);
            Files.walkFileTree(getPath().resolve(root), ImmutableSet.of(FileVisitOption.FOLLOW_LINKS), maxDepth - depth, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if(!isExcluded(dir)) {
                        if(MapFolder.isMapFolder(dir)) {
                            mapFolders.add(dir);
                        }
                        return FileVisitResult.CONTINUE;
                    } else {
                        logger.fine("Skipping excluded path " + dir);
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
            });
        }
        return mapFolders;
    }
}
