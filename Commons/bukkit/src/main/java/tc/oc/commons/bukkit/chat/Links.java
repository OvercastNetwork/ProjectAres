package tc.oc.commons.bukkit.chat;

import java.net.URI;
import java.net.URISyntaxException;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.util.ExceptionUtils;

public class Links {
    private Links() {}

    public static final String HOST = "localhost"; // TODO: configurable

    public static URI homeUri(String path) throws URISyntaxException {
        return new URI("http", HOST, path, null);
    }

    public static URI homeUriSafe(String path) {
        return ExceptionUtils.propagate(() -> homeUri(path));
    }

    public static BaseComponent homeLink(String path, boolean compact) throws URISyntaxException {
        return new LinkComponent(homeUri(path), compact);
    }

    public static BaseComponent homeLinkSafe(String path) {
        return homeLinkSafe(path, true);
    }

    public static BaseComponent homeLinkSafe(String path, boolean compact) {
        return ExceptionUtils.propagate(() -> homeLink(path, compact));
    }

    public static BaseComponent homeLink() {
        return homeLinkSafe("/");
    }

    public static BaseComponent shopLink() {
        return homeLinkSafe("/shop");
    }

    public static BaseComponent appealLink() {
        return homeLinkSafe("/appeal");
    }

    public static BaseComponent rulesLink() {
        return homeLinkSafe("/rules");
    }

    public static BaseComponent shopPlug(String perk, Object... with) {
        return new Component(ChatColor.LIGHT_PURPLE)
            .extra(new TranslatableComponent(perk, with))
            .extra(new Component(" "))
            .extra(shopLink());
    }

    public static URI profileUri(String username) {
        return homeUriSafe("/" + username);
    }

    public static URI profileUri(PlayerId playerId) {
        return homeUriSafe(playerId.username());
    }

    public static BaseComponent profileLink(String username) {
        return homeLinkSafe("/" + username);
    }

    public static BaseComponent profileLink(PlayerId playerId) {
        return profileLink(playerId.username());
    }
}
