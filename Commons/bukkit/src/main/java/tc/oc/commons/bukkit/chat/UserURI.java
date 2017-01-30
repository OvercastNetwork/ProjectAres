package tc.oc.commons.bukkit.chat;

import java.net.URI;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.nick.Identity;

public class UserURI implements Renderable<URI> {

    private final Optional<Identity> identity;
    private final String path;

    public UserURI(Optional<Identity> identity, String path) {
        this.identity = identity;
        this.path = path.length() == 0 || path.startsWith("/") ? path : "/" + path;
    }

    public UserURI(Optional<Identity> identity) {
        this(identity, "");
    }

    public UserURI(String path) {
        this(Optional.empty(), path);
    }

    public UserURI() {
        this(Optional.empty());
    }

    @Override
    public URI render(ComponentRenderContext context, CommandSender viewer) {
        return Links.homeUriSafe("/" + identity.map(id -> id.getName(viewer))
                                               .orElseGet(viewer::getName) +
                                 path);
    }
}
