package tc.oc.api.minecraft.logging;

import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;

import net.kencochrane.raven.event.EventBuilder;
import net.kencochrane.raven.event.helper.EventBuilderHelper;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.DeployInfo;
import tc.oc.api.exceptions.ApiNotConnected;
import tc.oc.api.minecraft.config.MinecraftApiConfiguration;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.logging.BetterRaven;

/**
 * Tags Sentry events with the identity of the local server
 */
public class RavenServerTagger implements EventBuilderHelper, PluginFacet {

    private final MinecraftApiConfiguration config;
    private final Server server;

    @Inject RavenServerTagger(Optional<BetterRaven> raven, MinecraftApiConfiguration config, Server server) {
        this.config = config;
        this.server = server;
        raven.ifPresent(r -> r.addBuilderHelper(this));
    }

    @Override
    public void helpBuildingEvent(EventBuilder eventBuilder) {
        try {
            eventBuilder.addTag("datacenter", config.datacenter());
            eventBuilder.addTag("server", server.name());
            eventBuilder.addTag("server_slug", server.bungee_name());
            eventBuilder.addTag("family", server.family());
            eventBuilder.addTag("box", config.box());

            if(server.deploy_info() != null) {
                eventBuilder.addTag("nextgen_branch", server.deploy_info().nextgen().version().branch());
                eventBuilder.addTag("nextgen_commit", server.deploy_info().nextgen().version().commit());

                for(Map.Entry<String, DeployInfo.Version> pack : server.deploy_info().packages().entrySet()) {
                    eventBuilder.addTag(pack.getKey() + "_commit", pack.getValue().commit());
                }
            }
        } catch(ApiNotConnected e) {
            eventBuilder.addTag("server_id", config.serverId());
        }
    }
}
