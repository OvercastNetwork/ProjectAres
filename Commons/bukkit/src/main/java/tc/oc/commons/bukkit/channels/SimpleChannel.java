package tc.oc.commons.bukkit.channels;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventBus;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.Chat;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.ChatCreator;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.MultiAudience;
import tc.oc.commons.core.plugin.PluginFacet;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public abstract class SimpleChannel implements MultiAudience, Channel, PluginFacet {

    @Inject protected Audiences audiences;
    @Inject protected EventBus eventBus;
    @Inject protected BukkitUserStore userStore;
    @Inject protected ChatCreator chatCreator;
    @Inject protected IdentityProvider identityProvider;

    // Chat messages are sent to the local server before API verification.
    // This prevents downtime or lockup from stopping all local server chat.
    protected Cache<String, Boolean> chatCache = CacheBuilder.newBuilder()
                                                             .expireAfterWrite(10, TimeUnit.MINUTES)
                                                             .build();

    public abstract BaseComponent prefix();

    public abstract BaseComponent format(PlayerComponent player, String message);

    @Override
    public void sendMessage(BaseComponent message) {
        MultiAudience.super.sendMessage(new Component(prefix()).extra(message));
    }

    @Override
    public Stream<? extends Audience> audiences() {
        return Stream.of(audiences.filter(this::viewable));
    }

    @Override
    public void chat(@Nullable PlayerId playerId, String message) {
        final ChannelChatEvent event = new ChannelChatEvent(this, playerId, message);
        eventBus.callEvent(event);
        if(!event.isCancelled()) {
            chatCreator.chat(playerId, event.message(), type(), this::show);
        }
    }

    @Override
    public void show(Chat chat) {
        if(chatCache.getIfPresent(chat._id()) == null) {
            chatCache.put(chat._id(), true);
            sendMessage(format(
                new PlayerComponent(identityProvider.currentOrConsoleIdentity(chat.sender())),
                chat.message()
            ));
        }
    }

    @Override
    public void chat(CommandSender sender, String message) {
        chat(sender instanceof Player ? userStore.tryUser((Player) sender) : null, message);
    }

}
