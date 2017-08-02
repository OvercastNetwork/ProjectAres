package tc.oc.pgm.animation;

import com.google.common.collect.Range;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.map.MapRootParser;
import tc.oc.pgm.regions.CuboidValidation;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.property.DurationProperty;
import tc.oc.pgm.xml.property.NumberProperty;
import tc.oc.pgm.xml.property.PropertyBuilderFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnimationParser implements MapRootParser {

    @Inject FeatureDefinitionContext features;
    @Inject Document doc;
    @Inject AnimationDefinitionImpl.Factory animationDefinitionFactory;
    @Inject RegionParser regionParser;
    @Inject PropertyBuilderFactory<Duration, DurationProperty> durations;
    @Inject PropertyBuilderFactory<Integer, NumberProperty<Integer>> integers;

    @Override
    public void parse() throws InvalidXMLException {

        for(Element elFrame : XMLUtils.flattenElements(doc.getRootElement(), "animations", "frame")) {
            features.define(
                    elFrame,
                    new FrameDefinitionImpl(
                            XMLUtils.parseVector(elFrame.getAttribute("origin"), (Vector) null),
                            regionParser.property(elFrame, "region").validate(CuboidValidation.INSTANCE).required(),
                            XMLUtils.parseBoolean(elFrame.getAttribute("air"), false),
                            XMLUtils.parseBoolean(elFrame.getAttribute("clear"), true)
                    )
            );
        }

        for(Element elAnimation : XMLUtils.flattenElements(doc.getRootElement(), "animations", "animation")) {
            List<FrameDefinition> frames = new ArrayList<>();
            for (Node elFrames : Node.fromChildren(elAnimation, "frames")) {
                for (Node elFrame : Node.fromChildren(elFrames.asElement(), "frame")) {
                    frames.add(features.reference(Node.fromRequiredAttr(elFrame.asElement(), "id"), FrameDefinition.class));
                }
            }

            Duration after = durations.property(elAnimation, "after").required();
            Duration loop = durations.property(elAnimation, "loop").required();

            int count = integers.property(elAnimation, "count")
                    .range(Range.atLeast(1))
                    .infinity(true)
                    .optional(Integer.MAX_VALUE);

            final Optional<ImVector>
                    position = XMLUtils.parseVector(elAnimation, "location").optional(),
                    offset = XMLUtils.parseVector(elAnimation, "offset").optional();

            if(position.isPresent() && offset.isPresent()) {
                throw new InvalidXMLException("attributes 'location' and 'offset' cannot be used together", elAnimation);
            }

            features.define(elAnimation, animationDefinitionFactory.create(frames, after, loop, count, position));
        }
    }
}
