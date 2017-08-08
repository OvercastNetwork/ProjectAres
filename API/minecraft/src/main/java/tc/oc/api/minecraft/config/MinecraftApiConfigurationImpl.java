package tc.oc.api.minecraft.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.minecraft.api.configuration.Configuration;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class MinecraftApiConfigurationImpl implements MinecraftApiConfiguration {

    private final Configuration config;

    @Inject MinecraftApiConfigurationImpl(Configuration config) {
        this.config = config;
    }

    @Override
    public String serverId() {
        return checkNotNull(config.getString("server.id"));
    }

    @Override
    public String datacenter() {
        return checkNotNull(config.getString("server.datacenter"));
    }

    @Override
    public String box() {
        return checkNotNull(config.getString("server.box"));
    }

    @Override
    public ServerDoc.Role role() {
        return ServerDoc.Role.valueOf(config.getString("server.role").toUpperCase());
    }

    @Override
    public boolean publishIp() {
        return config.getBoolean("server.publishIp", true);
    }

    @Override
    public String primaryQueueName() {
        return "server." + serverId();
    }
}
