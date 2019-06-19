package tc.oc.commons.core.localization;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import tc.oc.commons.core.FileUtils;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Lazy;

/**
 * Tools for working with localized files i.e. files that have a seperate copy for each {@link Locale}.
 *
 * Such files live under a root folder, which contains a folder for each locale.
 * The structure of localized files is reproduced within each locale folder.
 */
@Singleton
public class LocalizedFileManager {

    private final Path rootPath;

    private final Lazy<Set<Locale>> available = Lazy.expiring(
        Duration.ofMinutes(1), this::availableLocalesFresh
    );

    @Inject private LocalizedFileManager(@Named("translations") Path rootPath) {
        this.rootPath = rootPath;
    }

    public Path rootPath() {
        return rootPath;
    }

    public Stream<Locale> match(Locale locale) {
        return Locales.match(locale, availableLocales());
    }

    public Set<Locale> availableLocales() {
        return available.get();
    }

    public Set<Locale> availableLocalesFresh() {
        try(final Stream<Path> dir = FileUtils.directoryStream(rootPath)) {
            return dir.map(path -> Locale.forLanguageTag(path.getFileName().toString()))
                      .collect(Collectors.toImmutableSet());
        } catch(IOException e) {
            return Collections.emptySet();
        }
    }

    public Path localizedPath(Locale locale) {
        return rootPath.resolve(Paths.get(locale.toLanguageTag()));
    }

    public Path localizedPath(Locale locale, Path path) {
        return localizedPath(locale).resolve(path);
    }
}
