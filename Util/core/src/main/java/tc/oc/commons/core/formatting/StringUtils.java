package tc.oc.commons.core.formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.sk89q.minecraft.util.commands.ChatColor;
import tc.oc.commons.core.LiquidMetal;

public class StringUtils {
    public static final int GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH = 55; // Will never wrap, even with the largest characters

    /**
     * Shorthand for listToEnglishCompound(list, "", "").
     *
     * @see #listToEnglishCompound(java.util.Collection, String, String)
     */
    public static String listToEnglishCompound(Collection<String> list) {
        return listToEnglishCompound(list, "", "");
    }

    /**
     * Converts a list of strings to a nice English list as a string.
     * <p/>
     * For example: In: ["Anxuiz", "MonsieurApple", "Plastix"] Out: "Anxuiz, MonsieurApple and Plastix"
     *
     * @param list   List of strings to concatenate.
     * @param prefix Prefix to add before each element in the resulting string.
     * @param suffix Suffix to add after each element in the resulting string.
     * @return String version of the list of strings.
     */
    public static String listToEnglishCompound(Collection<?> list, String prefix, String suffix) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Object str : list) {
            if (i != 0) {
                if (i == list.size() - 1) {
                    builder.append(" and ");
                } else {
                    builder.append(", ");
                }
            }
            builder.append(prefix).append(str).append(suffix);
            i++;
        }
        return builder.toString();
    }

    /**
     * Shorthand for listToCompound(list, and, "", "").
     *
     * @see #listToCompound(java.util.Collection, String, String, String)
     */
    public static final String listToCompound(Collection<String> list, String and) {
        return listToCompound(list, and, "", "");
    }

    /**
     * Converts a list of strings to a nice list as a string.
     * <p/>
     * For example: In: ["Anxuiz", "MonsieurApple", "Plastix"] Out: "Anxuiz, MonsieurApple {and} Plastix"
     *
     * @param list   List of strings to concatenate.
     * @param and    String to be used for "and"
     * @param prefix Prefix to add before each element in the resulting string.
     * @param suffix Suffix to add after each element in the resulting string.
     * @return String version of the list of strings.
     */
    public static String listToCompound(Collection<?> list, String and, String prefix, String suffix) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Object str : list) {
            if (i != 0) {
                if (i == list.size() - 1) {
                    builder.append(" ").append(and).append(" ");
                } else {
                    builder.append(", ");
                }
            }
            builder.append(prefix).append(str).append(suffix);
            i++;
        }
        return builder.toString();
    }

    public static @Nullable <T> T bestFuzzyMatch(String query, Iterable<T> choices, double threshold, Function<? super T, String> stringifier) {
        T result = null;
        for(T choice : choices) {
            if(threshold <= LiquidMetal.score(stringifier.apply(choice), query)) {
                if(result != null) return null; // multiple matches
                result = choice;
            }
        }
        return result;
    }

    public static @Nullable <T> T bestFuzzyMatch(String search, Iterable<T> choices, double threshold) {
        return bestFuzzyMatch(search, choices, threshold, Object::toString);
    }

    public static @Nullable <T> T bestFuzzyMatch(String query, Map<String, T> choices, double threshold) {
        return fuzzyMatch(query, choices, threshold)
            .orElse(null);
    }

    public static <T> Optional<T> fuzzyMatch(String query, Iterable<T> choices, double threshold, Function<? super T, String> stringifier) {
        return Optional.ofNullable(bestFuzzyMatch(query, choices, threshold, stringifier));
    }

    public static <T> Optional<T> fuzzyMatch(String query, Iterable<T> choices, double threshold) {
        return fuzzyMatch(query, choices, threshold, Object::toString);
    }

    public static <T> Optional<T> fuzzyMatch(String query, Map<String, T> choices, double threshold) {
        return fuzzyMatch(query, choices.entrySet(), threshold, Map.Entry::getKey)
            .map(Map.Entry::getValue);
    }

    /**
     * Sanitizes the provided message, removing any non-alphanumeric characters and swapping spaces with the specified
     * string.
     * <p/>
     * Examples: sanitize("Hello! :) How are you?", '-') --> "Hello--How-are-you" sanitize("I am great, thank you!",
     * '*') --> "I*am*great*thank*you"
     *
     * @param string       The message to be sanitized.
     * @param spaceReplace The string to be substituted for spaces.
     * @return The sanitized string.
     */
    public static String sanitize(String string, String spaceReplace) {
        return string.replaceAll("[^\\dA-Za-z ]", "").replaceAll("\\s+", spaceReplace);
    }

    public static String truncate(String text, int length) {
        return text.substring(0, Math.min(text.length(), length));
    }

    public static String removeEnd(String whole, String end) {
        return whole.endsWith(end) ? truncate(whole, whole.length() - end.length())
                                   : whole;
    }

    public static String substring(String text, int begin, int end) {
        return text.substring(Math.min(text.length(), begin), Math.min(text.length(), end));
    }

    public static String dashedChatMessage(String message, String dash, String dashPrefix) {
        return dashedChatMessage(message, dash, dashPrefix, null);
    }

    public static String dashedChatMessage(String message, String dash, String dashPrefix, String messagePrefix) {
        message = " " + message + " ";
        int dashCount = (GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH - ChatColor.stripColor(message).length() - 2) / (dash.length() * 2);
        String dashes = dashCount >= 0 ? Strings.repeat(dash, dashCount) : "";

        StringBuffer builder = new StringBuffer();
        if (dashCount > 0) {
            builder.append(dashPrefix).append(dashes).append(ChatColor.RESET);
        }

        if(messagePrefix != null) {
            builder.append(messagePrefix);
        }

        builder.append(message);

        if (dashCount > 0) {
            builder.append(ChatColor.RESET).append(dashPrefix).append(dashes);
        }

        return builder.toString();
    }

    public static List<String> complete(String prefix, Class<? extends Enum> enumClass) {
        return complete(prefix, Stream.of(enumClass.getEnumConstants()).map(c -> c.name().toLowerCase().replace('_', ' ')));
    }

    public static List<String> complete(String prefix, Collection<String> options) {
        return complete(prefix, options.stream());
    }

    public static List<String> complete(String prefix, Stream<String> options) {
        final String prefixLower = prefix.toLowerCase();
        final int pos = prefixLower.lastIndexOf(' ');
        final List<String> matches = new ArrayList<>();
        options.forEach(option -> {
            if(option.toLowerCase().startsWith(prefixLower)) {
                matches.add(pos == -1 ? option : option.substring(pos + 1));
            }
        });
        Collections.sort(matches);
        return matches;
    }

    public static String slugify(String text, char space) {
        return text.trim().toLowerCase().replaceAll("[^a-z0-9" + space + "]+", String.valueOf(space));
    }

    public static Optional<String> nonEmpty(@Nullable String s) {
        return s != null && s.length() > 0 ? Optional.of(s) : Optional.empty();
    }

    public static String pluralize(String singular) {
        // Incredible natural language processing technology
        return singular + "s";
    }

    public static boolean startsWithIgnoreCase(String whole, String prefix) {
        return whole.length() >= prefix.length() &&
               whole.substring(0, prefix.length()).equalsIgnoreCase(prefix);
    }

    public static boolean isBlank(@Nullable String s) {
        if(s == null) return true;
        for(int i = 0; i < s.length(); i++) {
            if(!Character.isWhitespace(s.charAt(i))) return false;
        }
        return true;
    }
}
