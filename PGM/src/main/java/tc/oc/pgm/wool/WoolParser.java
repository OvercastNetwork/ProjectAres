package tc.oc.pgm.wool;

import javax.inject.Inject;

import org.bukkit.DyeColor;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;
import org.jdom2.Element;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.pgm.features.FeatureDefinitionParser;
import tc.oc.pgm.features.FeatureParser;
import tc.oc.pgm.goals.ProximityMetric;
import tc.oc.pgm.map.MapProto;
import tc.oc.pgm.map.ProtoVersions;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.property.PropertyBuilderFactory;

public class WoolParser implements FeatureDefinitionParser<MonumentWoolFactory> {

    @Inject private @MapProto SemanticVersion proto;
    @Inject private FeatureParser<TeamFactory> teamParser;
    @Inject private RegionParser regionParser;
    @Inject private PropertyBuilderFactory<Boolean, ?> booleans;
    @Inject private PropertyBuilderFactory<ImVector, ?> vectors;
    @Inject private PropertyBuilderFactory<DyeColor, ?> dyeColors;

    @Inject private WoolParser() {}

    @Override
    public MonumentWoolFactory parseElement(Element el) throws InvalidXMLException {
        // The default location is at infinity, so players/blocks are always an infinite distance from it
        final Vector location = proto.isOlderThan(ProtoVersions.WOOL_LOCATIONS)
                                ? new Vector(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
                                : vectors.property(el, "location").required();

        return new MonumentWoolFactoryImpl(
            booleans.property(el, "required").optional(null),
            booleans.property(el, "show").optional(true),
            teamParser.property(el, "owner")
                      .alias("team")
                      .required(),
            ProximityMetric.parse(el, "wool", new ProximityMetric(ProximityMetric.Type.CLOSEST_KILL, false)),
            ProximityMetric.parse(el, "monument", new ProximityMetric(ProximityMetric.Type.CLOSEST_BLOCK, false)),
            dyeColors.property(el, "color").required(),
            location,
            regionParser.property(el, "monument")
                        .legacy()
                        .union(),
            booleans.property(el, "craftable").optional(true)
        );
    }
}
