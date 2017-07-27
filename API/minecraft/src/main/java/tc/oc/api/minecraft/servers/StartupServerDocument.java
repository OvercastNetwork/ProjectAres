package tc.oc.api.minecraft.servers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.gson.Gson;
import tc.oc.api.docs.virtual.DeployInfo;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.util.Lazy;
import tc.oc.minecraft.api.plugin.PluginFinder;
import tc.oc.minecraft.api.server.LocalServer;

@Singleton
public class StartupServerDocument implements ServerDoc.Startup {

    @Inject private Gson gson;
    @Inject private LocalServer minecraftServer;
    @Inject private PluginFinder pluginFinder;

    private Logger logger;
    @Inject void init(Loggers loggers) {
        logger = loggers.get(getClass());
    }

    private final Lazy<Map<String, String>> pluginVersions = Lazy.from(() -> {
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put(minecraftServer.getName(), minecraftServer.getVersion());
        pluginFinder.getAllPlugins().forEach(
            plugin -> builder.put(plugin.getDescription().getName(), plugin.getDescription().getVersion())
        );
        return builder.build();
    });

    private final Lazy<DeployInfo> deployInfo = Lazy.from(() -> {
        final Path file = minecraftServer.getRootPath().resolve("deploy.json");
        try {
            return gson.fromJson(Files.newReader(file.toFile(), Charsets.UTF_8), DeployInfo.class);
        } catch(FileNotFoundException e) {
            logger.warning("Missing " + file);
            return null;
        }
    });

    private final Lazy<String> ip = Lazy.from(() -> {
       try {
           URL url = new URL("http://checkip.amazonaws.com");
           return new BufferedReader(new InputStreamReader(url.openStream())).readLine();
       } catch(IOException e) {
           logger.log(Level.SEVERE, "Unable to find external ip", e);
           return minecraftServer.getAddress().getHostName();
       }
    });

    @Override public boolean online() {
        return true;
    }

    @Override
    public String ip() {
        return ip.get();
    }

    @Override public Integer current_port() {
        return minecraftServer.getAddress().getPort();
    }

    @Override public @Nullable DeployInfo deploy_info() {
        return deployInfo.get();
    }

    @Override public Map<String, String> plugin_versions() {
        return pluginVersions.get();
    }

    @Override public Set<Integer> protocol_versions() {
        return minecraftServer.getProtocolVersions();
    }
}
