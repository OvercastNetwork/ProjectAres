package tc.oc.commons.core.chat;

import tc.oc.minecraft.api.command.CommandSender;
import tc.oc.minecraft.api.entity.Player;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * A factory to create {@link Audience}s for various purposes.
 */
public interface Audiences<C extends CommandSender> {

    Audience get(@Nullable C sender);

    MultiAudience filter(Predicate<C> condition);

    default MultiAudience playerFilter(Predicate<Player> condition) {
        return filter(sender -> sender instanceof Player && condition.test((Player) sender));
    }

    default MultiAudience permission(@Nullable String permission) {
        return filter(sender -> permission == null || permission.isEmpty() || sender.hasPermission(permission));
    }

    MultiAudience all();

    Audience console();

}
