package tc.oc.minecraft.analytics;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import tc.oc.analytics.Tag;
import tc.oc.analytics.TagSetBuilder;
import tc.oc.analytics.Tagger;
import tc.oc.api.docs.Server;
import tc.oc.api.minecraft.servers.LocalServerReconfigureEvent;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.Lazy;

@Singleton
public class ServerTagger implements Tagger, PluginFacet {

    private final Lazy<ImmutableSet<Tag>> tags;

    @Inject ServerTagger(Server server) {
        this.tags = Lazy.from(() -> {
            final TagSetBuilder builder = new TagSetBuilder();
            builder
                .add("server", server.slug())
                .add("datacenter", server.datacenter())
                .add("box", server.box())
                .add("network", server.network().name().toLowerCase())
                .add("role", server.role().name().toLowerCase())
                .add("visibility", server.visibility().name().toLowerCase())
                .add("family", server.family())
                .addAll("realm", server.realms());

            if(server.game_id() != null) {
                builder.add("game", server.game_id());
            }

            return builder.build();
        });
    }

    @Override
    public ImmutableSet<Tag> tags() {
        return tags.get();
    }

    @Subscribe
    void reconfigure(LocalServerReconfigureEvent event) {
        tags.clear();
    }
}
