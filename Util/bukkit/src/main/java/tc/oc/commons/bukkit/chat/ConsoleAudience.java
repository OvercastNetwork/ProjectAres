package tc.oc.commons.bukkit.chat;

import javax.inject.Inject;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Server;
import tc.oc.commons.core.chat.AbstractConsoleAudience;

public class ConsoleAudience extends AbstractConsoleAudience {

    private final Server server;

    @Inject public ConsoleAudience(Server server) {
        this.server = server;
    }

    @Override
    public void sendMessage(BaseComponent message) {
        ComponentRenderers.send(server.getConsoleSender(), message);
    }

    @Override
    public void sendMessage(String message) {
        server.getConsoleSender().sendMessage(message);
    }
}
