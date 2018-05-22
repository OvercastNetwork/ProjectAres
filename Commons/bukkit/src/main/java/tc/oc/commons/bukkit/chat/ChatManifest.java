package tc.oc.commons.bukkit.chat;

import tc.oc.commons.bukkit.broadcast.BroadcastSender;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class ChatManifest extends HybridManifest {
    @Override
    protected void configure() {

        expose(ChatCreator.class);
        expose(ChatAnnouncer.class);
        expose(BroadcastSender.class);

        final PluginFacetBinder facets = new PluginFacetBinder(binder());
        facets.register(ChatCreator.class);
        facets.register(ChatAnnouncer.class);
        facets.register(BroadcastSender.class);
    }
}
