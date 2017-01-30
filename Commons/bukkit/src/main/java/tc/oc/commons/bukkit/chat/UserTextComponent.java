package tc.oc.commons.bukkit.chat;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.core.chat.Component;

/**
 * Subclass of {@link TextComponent} used to display user-entered text.
 *
 * Current features:
 *  - Autolinking
 *
 * Also stores the {@link Identity} of the author, but we currently don't
 * use that for anything.
 *
 * TODO: Possible future features include linking/decorating player names,
 * masking offensive language, markdown formatting.
 */
public class UserTextComponent extends TextComponent {

    // Source: http://stackoverflow.com/a/5713866/6342
    private static final Pattern URL = Pattern.compile("(?:^|[\\W])(https?://|www\\.)" +
                                                       "(([\\w\\-]+\\.)+?([\\w\\-.~]+/?)*" +
                                                       "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
                                                       Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    final Optional<Identity> author;
    final Component rendered = new Component();

    public UserTextComponent(Optional<Identity> author, String text) {
        super(text);
        this.author = author;

        final Matcher matcher = URL.matcher(text);
        int textStart = 0;

        while(matcher.find()) {
            final int linkStart = matcher.start(1);
            final int linkEnd = matcher.end();

            if(linkStart > textStart) {
                rendered.extra(text.substring(textStart, linkStart));
            }

            final String link = text.substring(linkStart, linkEnd);
            final String url = link.startsWith("http") ? link
                                                       : "http://" + link;
            rendered.extra(new Component(link, ChatColor.BLUE, ChatColor.UNDERLINE).clickEvent(ClickEvent.Action.OPEN_URL, url));

            textStart = linkEnd;
        }

        if(textStart < text.length()) {
            rendered.extra(text.substring(textStart));
        }
    }

    public UserTextComponent(Identity author, String text) {
        this(Optional.of(author), text);
    }

    public UserTextComponent(String text) {
        this(Optional.empty(), text);
    }
}
