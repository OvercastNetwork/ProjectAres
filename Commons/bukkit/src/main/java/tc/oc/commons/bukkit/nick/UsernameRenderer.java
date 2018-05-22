package tc.oc.commons.bukkit.nick;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.commons.bukkit.chat.NameFlag;
import tc.oc.commons.bukkit.chat.PartialNameRenderer;
import tc.oc.commons.bukkit.chat.NameType;
import tc.oc.commons.core.chat.Component;

@Singleton
public class UsernameRenderer implements PartialNameRenderer {

    public static final ChatColor OFFLINE_COLOR = ChatColor.DARK_AQUA;
    public static final ChatColor ONLINE_COLOR = ChatColor.AQUA;
    public static final ChatColor DEAD_COLOR = ChatColor.DARK_GRAY;

    @Inject protected UsernameRenderer() {}

    public String getTextName(Identity identity, NameType type) {
        if(identity.getNickname() != null && !type.reveal) {
            return identity.getNickname();
        } else {
            return identity.getRealName();
        }
    }

    public ChatColor getColor(Identity identity, NameType type) {
        return type.dead && type.style.contains(NameFlag.DEATH) ? DEAD_COLOR : type.online ? ONLINE_COLOR : OFFLINE_COLOR;
    }

    @Override
    public String getLegacyName(Identity identity, NameType type) {
        String name = getTextName(identity, type);
        final String color = type.style.contains(NameFlag.COLOR) ? getColor(identity, type).toString() : "";
        String format = color;

        if(type.style.contains(NameFlag.SELF) && type.self && type.reveal) {
            format += ChatColor.BOLD;
        }

        if(type.style.contains(NameFlag.FRIEND) && type.friend && type.reveal) {
            format += ChatColor.ITALIC;
        }

        if(type.style.contains(NameFlag.DISGUISE) && identity.getNickname() != null && type.reveal) {
            format += ChatColor.STRIKETHROUGH;

            if(type.style.contains(NameFlag.NICKNAME)) {
                name += ChatColor.RESET + " " + color + ChatColor.ITALIC + identity.getNickname();
            }
        }

        return format + name;
    }

    @Override
    public BaseComponent getComponentName(Identity identity, NameType type) {
        Component rendered = new Component(getTextName(identity, type));

        if(type.style.contains(NameFlag.SELF) && type.self && type.reveal) {
            rendered.setBold(true);
        }

        if(type.style.contains(NameFlag.FRIEND) && type.friend && type.reveal) {
            rendered.setItalic(true);
        }

        if(type.style.contains(NameFlag.DISGUISE) && identity.getNickname() != null && type.reveal) {
            rendered.setStrikethrough(true);

            if(type.style.contains(NameFlag.NICKNAME)) {
                rendered = new Component(rendered, new Component(" " + identity.getNickname(), ChatColor.ITALIC));
            }
        }

        if(type.style.contains(NameFlag.COLOR)) {
            rendered.setColor(getColor(identity, type));
        }

        if(!identity.isConsole() && type.style.contains(NameFlag.TELEPORT)) {
            Component dupe = rendered.duplicate();
            rendered.clickEvent(makeRemoteTeleportClickEvent(identity, identity.getNickname() != null && !type.reveal));
            rendered.hoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("tip.teleportTo", dupe));
        }

        return rendered;
    }

    public ClickEvent makeRemoteTeleportClickEvent(@Nullable String traveler, String destination) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rtp " + (traveler == null ? "" : traveler + " ") + destination);
    }

    public ClickEvent makeRemoteTeleportClickEvent(Identity destination, boolean useNick) {
        return makeRemoteTeleportClickEvent(null, useNick ? destination.getNickname() : destination.getRealName());
    }
}
