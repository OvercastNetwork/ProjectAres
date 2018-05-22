package tc.oc.commons.bukkit.channels.server;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import tc.oc.api.docs.Chat;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.commons.bukkit.channels.SimpleChannel;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.core.chat.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ServerChannel extends SimpleChannel {

    @Inject ServerChannel() {}

    @Override
    public BaseComponent prefix() {
        return new Component();
    }

    @Override
    public BaseComponent format(Chat chat, PlayerComponent sender, String message) {
        return new Component().extra("<").extra(sender).extra(">: ").extra(message);
    }

    @Override
    public ChatDoc.Type type() {
        return ChatDoc.Type.SERVER;
    }

    @Override
    public boolean sendable(CommandSender sender) {
        return true;
    }

    @Override
    public boolean viewable(CommandSender sender) {
        return true;
    }

}
