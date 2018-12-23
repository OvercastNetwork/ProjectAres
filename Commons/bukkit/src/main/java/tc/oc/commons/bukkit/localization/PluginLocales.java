package tc.oc.commons.bukkit.localization;

import java.util.Locale;
import javax.annotation.Nullable;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.localization.LocaleMatcher;
import tc.oc.commons.core.localization.Locales;

public final class PluginLocales {
    private PluginLocales() {}

    private static final LocaleMatcher LOCALE_MATCHER = new LocaleMatcher(Locales.DEFAULT_LOCALE,
                                                                          Translations.get().supportedLocales());

    public static Locale locale(@Nullable CommandSender sender) {
        return LOCALE_MATCHER.closestMatchFor(Locales.locale(sender));
    }
}
