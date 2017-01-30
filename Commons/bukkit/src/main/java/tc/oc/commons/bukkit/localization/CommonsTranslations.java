package tc.oc.commons.bukkit.localization;

import tc.oc.commons.core.localization.TranslationSet;

public class CommonsTranslations extends PluginTranslations {
    private static CommonsTranslations instance;

    private CommonsTranslations() {
        super(new TranslationSet("commons.Commons"));
        instance = this;
    }

    public static CommonsTranslations get() {
        return instance == null ? new CommonsTranslations() : instance;
    }

}
