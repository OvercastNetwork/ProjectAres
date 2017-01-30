package tc.oc.pgm.modes;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

import com.google.inject.ImplementedBy;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.material.MaterialData;
import tc.oc.commons.bukkit.localization.BukkitTranslator;
import tc.oc.commons.bukkit.localization.MessageTemplate;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.map.MapRootParser;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.validate.DurationIs;
import tc.oc.pgm.xml.validate.MaterialDataIs;

@FeatureInfo(name = "mode")
@ImplementedBy(ObjectiveModeImpl.class)
public interface ObjectiveMode extends FeatureDefinition {

    @Property
    @Validate(DurationIs.NotNegative.class)
    Duration after();

    @Property
    @Validate(DurationIs.NotNegative.class)
    default Duration show_before() {
        return boss_bar() ? Duration.ofSeconds(60) : Duration.ZERO;
    }

    @Property
    @Legacy
    default boolean boss_bar() {
        return true;
    }

    @Property
    @Validate(MaterialDataIs.Block.class)
    MaterialData material();

    @Property
    Optional<MessageTemplate> name();

    BaseComponent materialName();
}

abstract class ObjectiveModeImpl extends FeatureDefinition.Impl implements ObjectiveMode {

    private BaseComponent materialName;

    @Inject private void init(BukkitTranslator bukkitTranslator) throws InvalidXMLException {
        materialName = new TranslatableComponent(
            bukkitTranslator.materialKey(material())
                            .orElseThrow(() -> new InvalidXMLException("No localized name for material " + material()))
        );
    }

    @Override
    public BaseComponent materialName() {
        return materialName;
    }
}

class ObjectiveModeValidator implements MapRootParser {

    final List<ObjectiveMode> modes;
    final FeatureDefinitionContext features;

    @Inject ObjectiveModeValidator(List<ObjectiveMode> modes, FeatureDefinitionContext features) {
        this.modes = modes;
        this.features = features;
    }

    @Override
    public void parse() throws InvalidXMLException {
        final Set<Duration> times = new HashSet<>();
        for(ObjectiveMode mode : modes) {
            if(!times.add(mode.after())) {
                throw new InvalidXMLException("Duplicate mode change time",
                                              features.definitionNode(mode));
            }
        }
    }
}