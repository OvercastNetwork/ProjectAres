package tc.oc.commons.core.localization;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.commons.core.util.Pair;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a single message resource file across all languages
 */
public class TranslationSet {

    private final String name;
    private final LoadingCache<Locale, Optional<ResourceBundle>> bundles;
    private final LoadingCache<Pair<String, Locale>, Optional<MessageFormat>> messages;

    public TranslationSet(String name) {
        this.name = checkNotNull(name);

        bundles = CacheUtils.newCache(locale -> {
            try {
                return Optional.of(ResourceBundle.getBundle(name, locale, new ResourceBundle.Control() {
                    @Override
                    public Locale getFallbackLocale(String baseName, Locale locale) {
                        // This prevents the OS locale from being used instead of Locale.ENGLISH
                        return null;
                    }
                }));
            } catch(MissingResourceException e) {
                return Optional.empty();
            }
        });
        bundles.refresh(Locales.DEFAULT_LOCALE);

        messages = CacheUtils.newCache(key -> {
            try {
                return bundles.getUnchecked(key.second)
                              .map(bundle -> Formats.quotedMessage(bundle.getString(key.first), key.second));
            } catch(MissingResourceException e) {
                return Optional.empty();
            }
        });
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return this == that || (that instanceof TranslationSet &&
                                this.name.equals(((TranslationSet) that).name));
    }

    public boolean hasLocale(Locale locale) {
        return bundles.getUnchecked(locale).isPresent();
    }

    public boolean hasKey(Locale locale, String key) {
        return pattern(key, locale).isPresent();
    }

    public boolean hasKey(String key) {
        return hasKey(Locales.DEFAULT_LOCALE, key);
    }

    public Set<String> getKeys(Locale locale) {
        return bundles.getUnchecked(locale)
                      .map(ResourceBundle::keySet)
                      .orElse(ImmutableSet.of());
    }

    public Set<String> getKeys() {
        return getKeys(Locales.DEFAULT_LOCALE);
    }

    public Optional<MessageFormat> pattern(String key, Locale locale) {
        return messages.getUnchecked(Pair.of(key, locale));
    }

    public @Nullable String render(String key, Locale locale, Object... arguments) {
        return pattern(key, locale).map(message -> message.format(arguments))
                                   .orElse(null);
    }
}
