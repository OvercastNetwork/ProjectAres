package tc.oc.commons.bukkit.chat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.ImmutableComponent;

public class LinkComponent extends ImmutableComponent implements RenderableComponent {

    private final Renderable<URI> uri;
    private final Optional<BaseComponent> content;
    private final boolean compact;

    public LinkComponent(Renderable<URI> uri, Optional<BaseComponent> content) {
        this.uri = uri;
        this.content = content;
        this.compact = true;
    }

    public LinkComponent(Renderable<URI> uri, boolean compact) {
        this.uri = uri;
        this.content = Optional.empty();
        this.compact = compact;
    }

    public LinkComponent(Renderable<URI> uri) {
        this(uri, true);
    }

    public LinkComponent(URI uri, Optional<BaseComponent> content) {
        this(Renderable.of(uri), content);
    }

    public LinkComponent(URI uri, boolean compact) {
        this(Renderable.of(uri), compact);
    }

    public LinkComponent(URI uri) {
        this(uri, true);
    }

    public LinkComponent(String uri, Optional<BaseComponent> content) throws URISyntaxException {
        this(new URI(uri), content);
    }

    public LinkComponent(String uri, boolean compact) throws URISyntaxException {
        this(new URI(uri), compact);
    }

    public LinkComponent(String uri) throws URISyntaxException {
        this(new URI(uri));
    }

    public LinkComponent(String scheme, String host, String path, Optional<BaseComponent> content) throws URISyntaxException {
        this(new URI(scheme, host, path, null), content);
    }

    public LinkComponent(String scheme, String host, String path, boolean compact) throws URISyntaxException {
        this(new URI(scheme, host, path, null), compact);
    }

    public LinkComponent(String scheme, String host, String path) throws URISyntaxException {
        this(new URI(scheme, host, path, null));
    }

    @Override
    public BaseComponent render(ComponentRenderContext context, CommandSender viewer) {
        final URI uri = this.uri.render(context, viewer);
        return new Component(context.render(content.orElseGet(() -> displayLink(uri)), viewer),
                             ChatColor.BLUE, ChatColor.UNDERLINE)
            .clickEvent(ClickEvent.Action.OPEN_URL, uri.toString());
    }

    private BaseComponent displayLink(URI uri) {
        String display = uri.getHost();

        // Don't append the path if it's just "/"
        // Use the raw path with illegal chars, which tends to look nicer.
        if(!"/".equals(uri.getRawPath())) {
            display = display + uri.getRawPath();
        }

        if(!compact) {
            display = uri.getScheme() + "://" + display;
        }
        return new Component(display);
    }

    @Override
    public BaseComponent duplicateWithoutFormatting() {
        return duplicate();
    }
}
