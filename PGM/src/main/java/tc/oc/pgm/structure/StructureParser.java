package tc.oc.pgm.structure;

import java.util.Optional;
import javax.inject.Inject;

import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.map.MapProto;
import tc.oc.pgm.map.MapRootParser;
import tc.oc.pgm.map.ProtoVersions;
import tc.oc.pgm.regions.CuboidValidation;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class StructureParser implements MapRootParser {

    @Inject FeatureDefinitionContext features;
    @Inject @MapProto SemanticVersion proto;
    @Inject Document doc;
    @Inject FilterParser filterParser;
    @Inject RegionParser regionParser;
    @Inject DynamicDefinitionImpl.Factory dynamicDefinitionFactory;

    @Override
    public void parse() throws InvalidXMLException {
        if(proto.isOlderThan(ProtoVersions.FILTER_FEATURES)) return;

        for(Element elStruct : XMLUtils.flattenElements(doc.getRootElement(), "structures", "structure")) {
            features.define(
                elStruct,
                new StructureDefinitionImpl(
                    XMLUtils.parseVector(elStruct.getAttribute("origin"), (Vector) null),
                    regionParser.property(elStruct, "region").validate(CuboidValidation.INSTANCE).required(),
                    XMLUtils.parseBoolean(elStruct.getAttribute("air"), false),
                    XMLUtils.parseBoolean(elStruct.getAttribute("clear"), true)
                )
            );
        }

        for(Element elDynamic : XMLUtils.flattenElements(doc.getRootElement(), "structures", "dynamic")) {
            final Optional<ImVector>
                position = XMLUtils.parseVector(elDynamic, "location").optional(),
                offset = XMLUtils.parseVector(elDynamic, "offset").optional();

            if(position.isPresent() && offset.isPresent()) {
                throw new InvalidXMLException("attributes 'location' and 'offset' cannot be used together", elDynamic);
            }

            final StructureDefinition structure = features.reference(Node.fromRequiredAttr(elDynamic, "structure"), StructureDefinition.class);

            final Filter trigger, filter;
            if(proto.isOlderThan(ProtoVersions.DYNAMIC_FILTERS)) {
                // Legacy maps use "filter" as the trigger
                trigger = filterParser.property(elDynamic, "filter")
                                      .respondsTo(IMatchQuery.class)
                                      .dynamic()
                                      .optional(StaticFilter.ALLOW);
                filter = StaticFilter.ALLOW;
            } else {
                // New maps have seperate "trigger" and "filter" properties
                trigger = filterParser.property(elDynamic, "trigger")
                                      .respondsTo(IMatchQuery.class)
                                      .dynamic()
                                      .optional(StaticFilter.ALLOW);
                filter = filterParser.property(elDynamic, "filter")
                                     .optional(StaticFilter.ALLOW);
            }

            features.define(elDynamic, dynamicDefinitionFactory.create(structure, trigger, filter, position, offset));
        }
    }
}
