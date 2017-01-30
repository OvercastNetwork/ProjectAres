package tc.oc.lobby.bukkit;

import tc.oc.commons.bukkit.localization.PluginTranslations;
import tc.oc.commons.core.localization.TranslationSet;

public class LobbyTranslations extends PluginTranslations {
    private static LobbyTranslations instance;

    public static final TranslationSet ERRORS = new TranslationSet("lobby.LobbyErrors");
    public static final TranslationSet MESSAGES = new TranslationSet("lobby.LobbyMessages");
    public static final TranslationSet MISC = new TranslationSet("lobby.LobbyMiscellaneous");
    public static final TranslationSet UI = new TranslationSet("lobby.LobbyUI");

    public LobbyTranslations() {
        super(ERRORS, MESSAGES, MISC, UI);
        instance = this;
    }

    public static LobbyTranslations get() {
        return instance == null ? new LobbyTranslations() : instance;
    }
}
