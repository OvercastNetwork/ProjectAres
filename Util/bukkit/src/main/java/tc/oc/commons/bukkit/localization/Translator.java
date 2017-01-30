package tc.oc.commons.bukkit.localization;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.NavigableSet;
import java.util.Optional;
import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;

/**
 * Knows how to translate keys into text for specific viewers
 */
public interface Translator {

    /**
     * Does the given locale contain a localized pattern for the given key?
     */
    boolean hasKey(Locale locale, String key);

    /**
     * Does a localized pattern exist for the given key in any locale?
     */
    boolean hasKey(String key);

    /**
     * Return the keys of all messages localized to the given locale.
     * If a prefix is given, return only keys that begin with that prefix.
     */
    NavigableSet<String> getKeys(Locale locale, @Nullable String prefix);

    /**
     * Return the keys of all localized messages across all locales.
     * If a prefix is given, return only keys that begin with that prefix.
     */
    NavigableSet<String> getKeys(@Nullable String prefix);

    /**
     * Localize the given key for the given viewer, with the given arguments.
     */
    String t(String key, @Nullable CommandSender sender, Object... arguments);

    /**
     * Format a localized String with a formatting prefix (such as a ChatColor).
     * The prefix is inserted at the start of the formatted result, and after each argument.
     */
    String t(String format, String key, @Nullable CommandSender viewer, Object... arguments);

    Optional<MessageFormat> pattern(String key);

    Optional<MessageFormat> pattern(String key, Locale locale);

    /**
     * Lookup a localized pattern for the given key and viewer, returning
     * null if the viewer's locale does not contain the key.
     */
    Optional<MessageFormat> pattern(String key, CommandSender sender);
}
