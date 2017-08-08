package tc.oc.api.minecraft.config;

import tc.oc.api.config.ApiConfiguration;
import tc.oc.api.docs.virtual.ServerDoc;

public interface MinecraftApiConfiguration extends ApiConfiguration {

    String serverId();

    String datacenter();

    String box();

    ServerDoc.Role role();

    boolean publishIp();
}
