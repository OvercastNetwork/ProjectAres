package tc.oc.api.bungee.users;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tc.oc.api.minecraft.users.UserStore;

@Singleton
public class BungeeUserStore extends UserStore<ProxiedPlayer> implements OnlinePlayers {

    @Inject private ProxyServer proxy;

    @Override
    public int count() {
        return proxy.getOnlineCount();
    }
}
