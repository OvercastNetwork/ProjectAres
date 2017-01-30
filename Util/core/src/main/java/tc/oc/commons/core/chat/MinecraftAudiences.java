package tc.oc.commons.core.chat;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;

import tc.oc.minecraft.api.command.CommandSender;
import tc.oc.minecraft.api.server.LocalServer;

public abstract class MinecraftAudiences<T extends CommandSender> implements Audiences<T> {

    @Inject private LocalServer localServer;

    @Override
    public Audience localServer() {
        return new AbstractMultiAudience() {
            @Override
            protected Iterable<? extends Audience> getAudiences() {
                return Stream.concat(localServer.getOnlinePlayers().stream(),
                                     Stream.of(localServer.getConsoleSender()))
                             .map(player -> get((T) player))
                             .collect(Collectors.toSet());
            }
        };
    }

    @Override
    public Audience withPermission(String permission) {
        return new AbstractMultiAudience() {
            @Override
            protected Iterable<? extends Audience> getAudiences() {
                return Stream.concat(localServer.getOnlinePlayers().stream(),
                                     Stream.of(localServer.getConsoleSender()))
                             .filter(player -> player.hasPermission(permission))
                             .map(player -> get((T) player))
                             .collect(Collectors.toSet());
            }
        };
    }
}
