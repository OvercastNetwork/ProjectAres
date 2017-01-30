package tc.oc.commons.bukkit.localization;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.command.CommandSender;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.commons.core.localization.Locales;
import tc.oc.commons.core.localization.TranslationSet;

import static com.google.common.base.Preconditions.checkState;

/**
 * Contains translations for all plugins. This should be used instead of the
 * individual plugin classes.
 */
@Singleton
public class Translations extends PluginTranslations {
    private static Translations instance;

    @Inject Translations() {
        super(
            new TranslationSet("chatmoderator.ChatModeratorErrors"),
            new TranslationSet("chatmoderator.ChatModeratorMessages"),
            new TranslationSet("adminchat.AdminChatErrors"),
            new TranslationSet("adminchat.AdminChatMessages"),
            new TranslationSet("commons.Commons"),
            new TranslationSet("pgm.PGMErrors"),
            new TranslationSet("pgm.PGMMessages"),
            new TranslationSet("pgm.PGMMiscellaneous"),
            new TranslationSet("pgm.PGMUI"),
            new TranslationSet("pgm.PGMDeath"),
            new TranslationSet("lobby.LobbyErrors"),
            new TranslationSet("lobby.LobbyMessages"),
            new TranslationSet("lobby.LobbyMiscellaneous"),
            new TranslationSet("lobby.LobbyUI"),
            new TranslationSet("projectares.PAErrors"),
            new TranslationSet("projectares.PAMessages"),
            new TranslationSet("projectares.PAMiscellaneous"),
            new TranslationSet("projectares.PAUI"),
            new TranslationSet("raindrops.RaindropsMessages"),
            new TranslationSet("tourney.Tourney")
        );
        instance = this;
    }

    public static Translations get() {
        checkState(instance != null, Translations.class + " not initialized");
        return instance;
    }

    public Set<Locale> supportedLocales() {
        return Stream.concat(Stream.of(Locales.DEFAULT_LOCALE, new Locale("af", "ZA")),
                             Stream.of(Locale.getAvailableLocales()))
                     .filter(locale -> translationSets().stream().anyMatch(set -> set.hasLocale(locale)))
                     .collect(Collectors.toSet());
    }

    public static String gamemodeShortName(MapDoc.Gamemode gamemode) {
        return "map.gamemode.short." + gamemode.name();
    }

    public static String gamemodeLongName(MapDoc.Gamemode gamemode) {
        return "map.gamemode.long." + gamemode.name();
    }

    public String legacyList(CommandSender viewer, String format, String elementFormat, Collection<?> elements) {
        switch(elements.size()) {
            case 0: return "";
            case 1: return elementFormat + elements.iterator().next();
            case 2:
                Iterator<?> pair = elements.iterator();
                return t(format, "misc.list.pair", viewer, elementFormat + pair.next(), elementFormat + pair.next());
            default:
                Iterator<?> iter = elements.iterator();
                String a = t(format, "misc.list.start", viewer, elementFormat + iter.next(), elementFormat + iter.next());
                String b = elementFormat + iter.next();
                while(iter.hasNext()) {
                    a = t(format, "misc.list.middle", viewer, a, b);
                    b = elementFormat + iter.next();
                }
                return t(format, "misc.list.end", viewer, a, b);
        }
    }
}
