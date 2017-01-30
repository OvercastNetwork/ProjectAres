package tc.oc.pgm;

import tc.oc.commons.bukkit.localization.PluginTranslations;
import tc.oc.commons.core.localization.TranslationSet;
import tc.oc.pgm.match.MatchPlayer;

import javax.annotation.Nullable;

public final class PGMTranslations extends PluginTranslations {
    private static PGMTranslations instance;

    public static final TranslationSet ERRORS = new TranslationSet("pgm.PGMErrors");
    public static final TranslationSet MESSAGES = new TranslationSet("pgm.PGMMessages");
    public static final TranslationSet MISC = new TranslationSet("pgm.PGMMiscellaneous");
    public static final TranslationSet UI = new TranslationSet("pgm.PGMUI");
    public static final TranslationSet DEATH = new TranslationSet("pgm.PGMDeath");

    public PGMTranslations() {
        super(ERRORS, MESSAGES, MISC, UI, DEATH);
        instance = this;
    }

    public static String t(String key, @Nullable MatchPlayer player, @Nullable Object... arguments) {
        return get().t(key, player == null ? null : player.getBukkit(), arguments);
    }

    public static PGMTranslations get() {
        return instance == null ? new PGMTranslations() : instance;
    }
}
