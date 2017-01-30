package tc.oc.commons.core.localization;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import tc.oc.minecraft.api.command.CommandSender;
import tc.oc.minecraft.api.entity.Player;

public final class Locales {
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    /**
     * Attempts to retrieve the best locale for the specified {@link CommandSender}.
     * If a null sender is given, the default locale is returned.
     */
    public static Locale locale(@Nullable CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getCurrentLocale();
        } else {
            return DEFAULT_LOCALE;
        }
    }

    public static boolean isDefault(Locale locale) {
        return DEFAULT_LOCALE.equals(locale);
    }

    /**
     * Return all locales from the given set that can serve as substitutes
     * for the given locale, from best to worst.
     */
    public static Stream<Locale> match(Locale locale, Set<Locale> available) {
        final Stream.Builder<Locale> result = Stream.builder();

        // Exact match
        if(available.contains(locale)) {
            result.add(locale);
        }

        // Locale has a country, we have a matching language with no country
        if(!"".equals(locale.getCountry())) {
            final Locale language = new Locale(locale.getLanguage());
            if(available.contains(language)) {
                result.add(language);
            }
        }

        // Available locales with the same language and some other country
        for(Locale a : available) {
            if(!"".equals(a.getCountry()) &&
               !locale.getCountry().equals(a.getCountry()) &&
               locale.getLanguage().equals(a.getLanguage())) {

                result.add(a);
            }
        }

        // If the language matches the default locale, then it will already be added.
        // Otherwise, add it now.
        if(!locale.getLanguage().equals(DEFAULT_LOCALE.getLanguage())) {
            result.add(DEFAULT_LOCALE);
        }

        return result.build();
    }
}
