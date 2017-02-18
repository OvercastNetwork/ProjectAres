package tc.oc.pgm.map;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

import tc.oc.commons.core.util.Lazy;
import tc.oc.commons.core.util.Utils;
import tc.oc.image.ImageUtils;

public class MapFolder {

    public static final String MAP_DESCRIPTION_FILE_NAME = "map.xml";
    public static final String IMAGE_FILE_NAME = "map.png";
    public static final int THUMBNAIL_HEIGHT = 64;

    public static boolean isMapFolder(Path path) {
        return Files.isDirectory(path) && Files.isRegularFile(path.resolve(MAP_DESCRIPTION_FILE_NAME));
    }

    private final MapSource source;
    private final Path path;

    private final Lazy<Collection<Path>> images = Lazy.from(() -> {
        final Path path = getAbsolutePath().resolve(IMAGE_FILE_NAME);
        return Files.isRegularFile(path)? Collections.singleton(path)
                                        : Collections.emptySet();
    });

    private final Lazy<Optional<String>> thumbnailUri = Lazy.from(
        () -> getImages().stream().findAny()
                         .map(path -> ImageUtils.thumbnailUri(path, THUMBNAIL_HEIGHT))
    );

    public MapFolder(MapSource source, Path path) {
        this.source = source;
        this.path = path;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "{source=" + source +
               " path=" + getRelativePath() +
               "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, getRelativePath());
    }

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(MapFolder.class, this, obj, that ->
            this.getSource().equals(that.getSource()) &&
            this.getRelativePath().equals(that.getRelativePath())
        );
    }

    public MapSource getSource() {
        return source;
    }

    public Path getAbsolutePath() {
        return source.getPath().resolve(path);
    }

    public Path getRelativePath() {
        return source.getPath().relativize(path);
    }

    public Path getAbsoluteDescriptionFilePath() {
        return getAbsolutePath().resolve(MAP_DESCRIPTION_FILE_NAME);
    }

    public Path getRelativeDescriptionFilePath() {
        return getRelativePath().resolve(MAP_DESCRIPTION_FILE_NAME);
    }

    private @Nullable URL getRelativeUrl(Path path) {
        // Resolving a Path against a URL is surprisingly tricky, due to character escaping issues.
        // The safest approach seems to be appending the path components one at a time, wrapping
        // each one in a URI to ensure that the filename is properly escaped. Trying to append the
        // entire thing at once either fails to escape illegal chars at all, or escapes characters
        // that shouldn't be, like the path seperator.
        try {
            URL url = source.getUrl();
            if(url == null) return null;

            URI uri = url.toURI();

            if(uri.getPath() == null || "".equals(uri.getPath())) {
                uri = uri.resolve("/");
            }

            Path dir = Files.isDirectory(source.getPath().resolve(path)) ? path : path.getParent();
            if(dir == null) return null;
            for(Path part : dir) {
                uri = uri.resolve(new URI(null, null, part.toString() + "/", null));
            }
            if(path != dir) {
                uri = uri.resolve(new URI(null, null, path.getFileName().toString(), null));
            }

            return uri.toURL();
        } catch(MalformedURLException | URISyntaxException e) {
            return null;
        }
    }

    public @Nullable URL getUrl() {
        return getRelativeUrl(getRelativePath());
    }

    public @Nullable URL getDescriptionFileUrl() {
        return getRelativeUrl(getRelativeDescriptionFilePath());
    }

    public Optional<String> getThumbnailUri() {
        return thumbnailUri.get();
    }

    public Collection<Path> getImages() {
        return images.get();
    }
}
