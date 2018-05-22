package tc.oc.commons.bukkit.channels.admin;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.BooleanType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import tc.oc.api.docs.Chat;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.channels.PermissibleChannel;
import tc.oc.commons.bukkit.channels.SimpleChannel;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.format.ServerFormatter;
import tc.oc.commons.bukkit.permissions.PermissionRegistry;
import tc.oc.commons.bukkit.settings.SettingManagerProvider;
import tc.oc.commons.core.chat.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AdminChannel extends SimpleChannel implements PermissibleChannel {

    public final static Permission PERMISSION = new Permission("chat.admin", PermissionDefault.OP);
    public final static Setting SETTING = new SettingBuilder()
            .name("AdminChat")
            .alias("ac")
            .summary("Show confidential staff chat")
            .type(new BooleanType())
            .defaultValue(true)
            .get();

    private final SettingManagerProvider settings;
    private final PermissionRegistry permissions;
    private final Server localServer;
    private final ServerStore serverStore;

    @Inject AdminChannel(PermissionRegistry permissions, SettingManagerProvider settings, Server localServer, ServerStore serverStore) {
        this.settings = settings;
        this.permissions = permissions;
        this.localServer = localServer;
        this.serverStore = serverStore;
    }

    @Override
    public void enable() {
        permissions.register(PERMISSION);
    }

    @Override
    public Permission permission() {
        return PERMISSION;
    }

    @Override
    public boolean viewable(CommandSender sender) {
        return !(sender instanceof Player) ||
               (PermissibleChannel.super.viewable(sender) &&
               settings.getManager((Player) sender).getValue(SETTING, Boolean.class, true));
    }

    @Override
    public BaseComponent prefix() {
        return new Component().extra("[").extra(new Component("A", ChatColor.GOLD)).extra("] ");
    }

    @Override
    public BaseComponent format(Chat chat, PlayerComponent player, String message) {
        Component component = new Component();
        if(!localServer._id().equals(chat.server_id())) {
            final Server server = serverStore.byId(chat.server_id());
            component.extra(ServerFormatter.light.nameWithDatacenter(server)).extra(" ");
        }
        return component.extra(player).extra(": ").extra(message);
    }

    @Override
    public ChatDoc.Type type() {
        return ChatDoc.Type.ADMIN;
    }

}
