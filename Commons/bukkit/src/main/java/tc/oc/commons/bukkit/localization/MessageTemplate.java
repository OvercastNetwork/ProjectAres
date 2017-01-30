package tc.oc.commons.bukkit.localization;

import java.text.MessageFormat;
import java.util.Locale;
import javax.inject.Inject;

import org.bukkit.command.CommandSender;
import tc.oc.commons.core.localization.Locales;

/**
 * Encapsulates a localized message template across all languages.
 *
 * Can be converted to a {@link MessageFormat}s by providing a specific {@link Locale}.
 */
public interface MessageTemplate {

    /**
     * Is this template actually translated into different languages?
     */
    boolean isLocalized();

    MessageFormat format(Locale locale);

    default MessageFormat format() {
        return format(Locales.DEFAULT_LOCALE);
    }

    default MessageFormat format(CommandSender viewer) {
        return format(PluginLocales.locale(viewer));
    }

    class Factory {
        private final Translator translator;

        @Inject Factory(Translator translator) {
            this.translator = translator;
        }

        /**
         * Create a {@link MessageTemplate} that returns the given {@link MessageFormat} for all locales.
         */
        public MessageTemplate literal(MessageFormat message) {
            return new MessageTemplate() {
                @Override
                public boolean isLocalized() {
                    return false;
                }

                @Override
                public MessageFormat format(Locale locale) {
                    return message;
                }

                @Override
                public String toString() {
                    return MessageTemplate.class.getSimpleName() + "{text=" + message + "}";
                }
            };
        }

        /**
         * Create a localized {@link MessageTemplate} from the given message key.
         */
        public MessageTemplate fromKey(String key) {
            if(!translator.hasKey(key)) {
                throw new IllegalArgumentException("Missing translation key '" + key + "'");
            }
            return new MessageTemplate() {
                @Override
                public boolean isLocalized() {
                    return true;
                }

                @Override
                public MessageFormat format(Locale locale) {
                    return translator.pattern(key, locale).get();
                }

                @Override
                public String toString() {
                    return MessageTemplate.class.getSimpleName() + "{key=" + key + "}";
                }
            };
        }
    }
}
