package tc.oc.commons.core.configuration;

import java.io.InputStream;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

import org.yaml.snakeyaml.Yaml;

public class YamlConfiguration extends TestConfiguration {
    @Inject public YamlConfiguration(Yaml yaml, @Named("config.yml") InputStream content) {
        super("", (Map<String, Object>) yaml.load(content));
    }
}
