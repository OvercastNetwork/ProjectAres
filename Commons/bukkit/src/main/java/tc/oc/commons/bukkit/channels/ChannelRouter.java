package tc.oc.commons.bukkit.channels;

import com.google.common.util.concurrent.ListenableFuture;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.Chat;
import tc.oc.api.docs.User;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.channels.admin.AdminChannel;
import tc.oc.commons.bukkit.channels.server.ServerChannel;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.function.Function;

/**
 * Get the {@link Channel} based on the {@link ChatDoc.Type}.
 */
@Singleton
public class ChannelRouter {

    private final BukkitUserStore userStore;
    private final UserService userService;
    private final ServerChannel serverChannel;
    private final AdminChannel adminChannel;
    private Function<Player, Channel> teamChannelFunction;

    @Inject ChannelRouter(BukkitUserStore userStore, UserService userService, ServerChannel serverChannel, AdminChannel adminChannel) {
        this.userStore = userStore;
        this.userService = userService;
        this.serverChannel = serverChannel;
        this.adminChannel = adminChannel;
        setTeamChannelFunction(null);
    }

    public Optional<Channel> getChannel(ChatDoc.Type type) {
        return getChannel(null, type);
    }

    public Optional<Channel> getChannel(Chat chat) {
        return getChannel(userStore.find(chat.sender()), chat.type());
    }

    public Optional<Channel> getChannel(@Nullable CommandSender sender, ChatDoc.Type type) {
        Channel channel = null;
        if(type == ChatDoc.Type.SERVER) {
            channel = serverChannel;
        } else if(type == ChatDoc.Type.ADMIN) {
            channel = adminChannel;
        } else if(sender != null && sender instanceof Player && type == ChatDoc.Type.TEAM) {
            channel = teamChannelFunction.apply((Player) sender);
        }
        return Optional.ofNullable(channel);
    }

    public Channel getDefaultChannel() {
        return serverChannel;
    }

    public Channel getDefaultChannel(Player player) {
        return getChannel(player, userStore.getUser(player).chat_channel()).orElse(getDefaultChannel());
    }

    public ListenableFuture<User> setDefaultChannel(Player player, ChatDoc.Type type) {
        return userService.update(userStore.playerId(player), new UserDoc.Channel() {
            @Override
            public ChatDoc.Type chat_channel() {
                return type;
            }
        });
    }

    public void setTeamChannelFunction(@Nullable Function<Player, Channel> function) {
        teamChannelFunction = function != null ? function : sender -> serverChannel;
    }

}
