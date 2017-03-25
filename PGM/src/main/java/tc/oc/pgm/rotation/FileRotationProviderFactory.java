package tc.oc.pgm.rotation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.pgm.map.MapLibrary;

public class FileRotationProviderFactory {

    private final MinecraftService minecraftService;

    @Inject FileRotationProviderFactory(MinecraftService minecraftService) {
        this.minecraftService = minecraftService;
    }

    public Set<RotationProviderInfo> parse(MapLibrary mapLibrary, Path dataPath, Configuration config) {
        ConfigurationSection base = config.getConfigurationSection("rotation.providers.file");
        if(base == null) return Collections.emptySet();

        Set<RotationProviderInfo> providers = new HashSet<>();
        for(String name : base.getKeys(false)) {
            ConfigurationSection provider = base.getConfigurationSection(name);

            Path rotationFile = Paths.get(provider.getString("path"));
            if(!rotationFile.isAbsolute()) rotationFile = dataPath.resolve(rotationFile);

            int priority = provider.getInt("priority", 0);
            int count = provider.getInt("count", 0);

            if(Files.isRegularFile(rotationFile)) {
                providers.add(new RotationProviderInfo(new FileRotationProvider(mapLibrary, name, rotationFile, dataPath), name, priority, count));
            } else if(minecraftService.getLocalServer().startup_visibility() == ServerDoc.Visibility.PUBLIC) {
                // This is not a perfect way to decide whether or not to throw an error, but it's the best we can do right now
                mapLibrary.getLogger().severe("Missing rotation file: " + rotationFile);
            }
        }

        return providers;
    }
}
