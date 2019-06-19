package tc.oc.pgm.rotation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.pgm.map.MapLibrary;

public class FileRotationProviderFactory {

    private final MinecraftService minecraftService;

    @Inject FileRotationProviderFactory(MinecraftService minecraftService) {
        this.minecraftService = minecraftService;
    }

    public Set<RotationProviderInfo> parse(MapLibrary mapLibrary, Path dataPath, Configuration config) {
        ConfigurationSection base = config.getConfigurationSection("rotation.providers");
        if(base == null) return Collections.emptySet();
        Set<RotationProviderInfo> providers = new HashSet<>();
        for(String pathString : base.getStringList("files")) {
            Path path = Paths.get(pathString);
            if(!path.isAbsolute()) path = dataPath.resolve(path);
            File file = path.toFile();
            if(file.isDirectory()) {
                try {
                    Files.walk(path)
                         .filter(Files::isRegularFile)
                         .filter(this::isYaml)
                         .map(Path::toFile)
                         .forEach(f -> providers.add(parse(mapLibrary, f, YamlConfiguration.loadConfiguration(f))));
                } catch(IOException e) {
                    e.printStackTrace();
                }
            } else if(file.isFile()) {
                providers.add(parse(mapLibrary, file, YamlConfiguration.loadConfiguration(file)));
            }
        }
        return providers;
    }

    public RotationProviderInfo parse(MapLibrary mapLibrary, File file, YamlConfiguration yaml) {
        String name = yaml.getString("name", "default");
        int priority = yaml.getInt("priority", 0);
        int count = yaml.getInt("count", 0);
        boolean shuffle = yaml.getBoolean("shuffle", false);
        Optional<ServerDoc.Rotation> next = minecraftService.getLocalServer()
                .rotations()
                .stream()
                .filter(rot -> rot.name().equals(name) && mapLibrary.getMapByNameOrId(rot.next_map_id()).isPresent())
                .findFirst();
        return new RotationProviderInfo(
            new FileRotationProvider(mapLibrary, name, Paths.get(file.getAbsolutePath()), next, shuffle),
            name, priority, count
        );
    }

    private boolean isYaml(Path path) {
        try {
            new YamlConfiguration().load(path.toFile());
            return true;
        } catch(Exception e) {
            return false;
        }
    }
}
