package tc.oc.commons.core.chat;

import com.google.common.collect.ImmutableList;
import tc.oc.minecraft.api.command.CommandSender;
import tc.oc.minecraft.api.server.LocalServer;

import javax.inject.Inject;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AbstractAudiences<C extends CommandSender> implements Audiences<C> {

    @Inject
    LocalServer localServer;

    @Override
    public MultiAudience all() {
        return () -> senders().map(sender -> get((C) sender));
    }

    @Override
    public MultiAudience filter(Predicate<C> condition) {
        return () -> senders().filter(sender -> condition.test((C) sender)).map(sender -> get((C) sender));
    }

    @Override
    public Audience console() {
        return get((C) localServer.getConsoleSender());
    }

    private Stream<CommandSender> senders() {
        return ImmutableList.<CommandSender>builder().add(localServer.getConsoleSender()).addAll(localServer.getOnlinePlayers()).build().stream();
    }

}
