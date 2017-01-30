package tc.oc.commons.bungee.chat;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.core.chat.AbstractConsoleAudience;

public class ConsoleAudience extends AbstractConsoleAudience {

    @Override
    public void sendMessage(BaseComponent message) {
        ProxyServer.getInstance().getConsole().sendMessage(message);
    }

    @Override
    public void sendMessage(String message) {
        ProxyServer.getInstance().getConsole().sendMessage(message);
    }
}
