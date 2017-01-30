package tc.oc.pgm.core;

import javax.inject.Inject;

import org.bukkit.Material;
import org.jdom2.Element;
import tc.oc.pgm.features.FeatureDefinitionParser;
import tc.oc.pgm.features.FeatureParser;
import tc.oc.pgm.goals.ProximityMetric;
import tc.oc.pgm.regions.BlockBoundedValidation;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.property.PropertyBuilderFactory;

public class CoreParser implements FeatureDefinitionParser<CoreFactory> {

    private final PropertyBuilderFactory<Boolean, ?> booleanParser;
    private final PropertyBuilderFactory<Integer, ?> intParser;
    private final FeatureParser<TeamFactory> teamParser;
    private final RegionParser regionParser;

    @Inject private CoreParser(PropertyBuilderFactory<Boolean, ?> booleanParser, PropertyBuilderFactory<Integer, ?> intParser, FeatureParser<TeamFactory> teamParser, RegionParser regionParser) {
        this.booleanParser = booleanParser;
        this.intParser = intParser;
        this.teamParser = teamParser;
        this.regionParser = regionParser;
    }

    @Override
    public CoreFactory parseElement(Element el) throws InvalidXMLException {
        return new CoreFactoryImpl(
            el.getAttributeValue("name", "Core"),
            booleanParser.property(el, "required").optional(null),
            booleanParser.property(el, "show").optional(true),
            teamParser.property(el, "owner")
                      .alias("team")
                      .required(),
            ProximityMetric.parse(el, new ProximityMetric(ProximityMetric.Type.CLOSEST_PLAYER, false)),
            regionParser.property(el)
                        .legacy()
                        .validate(BlockBoundedValidation.INSTANCE)
                        .union(),
            XMLUtils.parseBlockMaterialData(Node.fromAttr(el, "material"), Material.OBSIDIAN.getNewData((byte) 0)),
            intParser.property(el, "leak").optional(5),
            booleanParser.property(el, "mode-changes").optional(false)
        );
    }
}
