package tc.oc.pgm.listing;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;
import org.bukkit.command.CommandSender;
import tc.oc.api.message.types.Reply;

public interface ListingService {

    ListenableFuture<Reply> update(boolean online);

    ListenableFuture<Reply> update(boolean online, CommandSender sender);

    @Nullable String sessionDigest();
}
