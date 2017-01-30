package tc.oc.commons.bukkit.localization;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.localization.Locales;
import tc.oc.commons.core.localization.TranslationSet;
import tc.oc.commons.core.util.CacheUtils;

/**
 * Manages the {@link TranslationSet}s for a specific plugin.
 */
public abstract class PluginTranslations implements Translator {

    private final Set<TranslationSet> translationSets;
    private final LoadingCache<String, Optional<TranslationSet>> setsByKey;

    public PluginTranslations(TranslationSet... sets) {
        translationSets = ImmutableSet.copyOf(sets);
        setsByKey = CacheUtils.newCache(key -> translationSets.stream()
                                                              .filter(set -> set.hasKey(key))
                                                              .findFirst());
    }

    protected Set<TranslationSet> translationSets() {
        return translationSets;
    }

    @Override
    public boolean hasKey(Locale locale, String key) {
        for(TranslationSet translations : translationSets()) {
            if(translations.hasKey(locale, key)) return true;
        }
        return false;
    }

    @Override
    public boolean hasKey(String key) {
        return hasKey(Locales.DEFAULT_LOCALE, key);
    }

    @Override
    public NavigableSet<String> getKeys(Locale locale, @Nullable String prefix) {
        NavigableSet<String> keys = new TreeSet<>();
        for(TranslationSet translations : translationSets()) {
            for(String key : translations.getKeys(locale)) {
                if(prefix == null || key.startsWith(prefix)) keys.add(key);
            }
        }
        return keys;
    }

    @Override
    public NavigableSet<String> getKeys(@Nullable String prefix) {
        return getKeys(Locales.DEFAULT_LOCALE, prefix);
    }

    @Override
    public String t(String key, @Nullable CommandSender sender, Object... arguments) {
        return pattern(key, PluginLocales.locale(sender))
            .map(format -> format.format(arguments))
            .orElseGet(() -> "<translation '" + key + "' missing>");
    }

    @Override
    public String t(String format, String key, @Nullable CommandSender viewer, Object... arguments) {
        for(int i = 0; i < arguments.length; i++) {
            arguments[i] = String.valueOf(arguments[i]) + format;
        }
        return format + this.t(key, viewer, arguments);
    }

    @Override
    public Optional<MessageFormat> pattern(String key) {
        return pattern(key, Locales.DEFAULT_LOCALE);
    }

    @Override
    public Optional<MessageFormat> pattern(String key, Locale locale) {
        return setsByKey.getUnchecked(key)
                        .flatMap(set -> set.pattern(key, locale));
    }

    @Override
    public Optional<MessageFormat> pattern(String key, CommandSender sender) {
        return pattern(key, PluginLocales.locale(sender));
    }
}
