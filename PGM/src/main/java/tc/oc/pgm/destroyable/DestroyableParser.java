package tc.oc.pgm.destroyable;

import javax.inject.Inject;

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
import tc.oc.pgm.xml.property.PercentagePropertyFactory;
import tc.oc.pgm.xml.property.PropertyBuilderFactory;

public class DestroyableParser implements FeatureDefinitionParser<DestroyableFactory> {

    private final PropertyBuilderFactory<Boolean, ?> booleanParser;
    private final PercentagePropertyFactory percentages;
    private final FeatureParser<TeamFactory> teamParser;
    private final RegionParser regionParser;

    @Inject private DestroyableParser(PropertyBuilderFactory<Boolean, ?> booleanParser, FeatureParser<TeamFactory> teamParser, RegionParser regionParser, PercentagePropertyFactory percentages) {
        this.booleanParser = booleanParser;
        this.teamParser = teamParser;
        this.regionParser = regionParser;
        this.percentages = percentages;
    }

    @Override
    public DestroyableFactory parseElement(Element el) throws InvalidXMLException {
        return new DestroyableFactoryImpl(
            XMLUtils.getRequiredAttribute(el, "name").getValue(),
            booleanParser.property(el, "required").optional(null),
            booleanParser.property(el, "show").optional(true),
            teamParser.property(el, "owner").required(),
            ProximityMetric.parse(el, new ProximityMetric(ProximityMetric.Type.CLOSEST_PLAYER, false)),
            regionParser.property(el)
                        .legacy()
                        .validate(BlockBoundedValidation.INSTANCE)
                        .union(),
            XMLUtils.parseMaterialPatternSet(Node.fromRequiredAttr(el, "material", "materials")),
            percentages.property(el, "completion").optional(1D),
            booleanParser.property(el, "mode-changes").optional(false),
            booleanParser.property(el, "show-progress").optional(false),
            booleanParser.property(el, "sparks").optional(false),
            booleanParser.property(el, "repairable").optional(false)
        );
    }
}
