package tc.oc.pgm.tutorial;

import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jdom2.Element;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.pgm.points.PointParser;
import tc.oc.pgm.points.PointProvider;
import tc.oc.pgm.points.PointProviderAttributes;
import tc.oc.pgm.points.RandomPointProvider;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.parser.ElementParser;

public class TutorialParser implements ElementParser<Tutorial> {

    private final PointParser pointParser;

    @Inject private TutorialParser(PointParser pointParser) {
        this.pointParser = pointParser;
    }

    @Override
    public Tutorial parseElement(Element element) throws InvalidXMLException {
        List<TutorialStage> prefixStages = Lists.newArrayList();
        List<TutorialStage> stages = Lists.newArrayList();
        List<TutorialStage> suffixStages = Lists.newArrayList();

        for(Element tutorialEl : element.getChildren("tutorial")) {
            Element prefixEl = tutorialEl.getChild("prefix");
            if(prefixEl != null) {
                prefixStages.addAll(parseStages(pointParser, prefixEl));
            }

            Element suffixEl = tutorialEl.getChild("suffix");
            if(suffixEl != null) {
                suffixStages.addAll(parseStages(pointParser, suffixEl));
            }

            stages.addAll(parseStages(pointParser, tutorialEl));
        }

        stages.addAll(0, prefixStages);
        stages.addAll(suffixStages);

        return new Tutorial(stages);
    }

    private List<TutorialStage> parseStages(PointParser pointParser, Element parent) throws InvalidXMLException {
        List<TutorialStage> stages = Lists.newArrayList();

        for(Element stageEl : parent.getChildren("stage")) {
            String title = BukkitUtils.colorize(XMLUtils.getRequiredAttribute(stageEl, "title").getValue());

            List<String> message = parseMessage(stageEl);
            PointProvider teleport = parseTeleport(pointParser, stageEl);

            stages.add(new TutorialStage(title, message, teleport));
        }

        return stages;
    }

    private List<String> parseMessage(Element stageEl) {
        ImmutableList.Builder<String> builder = ImmutableList.builder();

        Element messageEl = stageEl.getChild("message");
        if(messageEl != null) {
            for(Element lineEl : messageEl.getChildren("line")) {
                builder.add(BukkitUtils.colorize(lineEl.getText()));
            }
        }

        return builder.build();
    }

    private PointProvider parseTeleport(PointParser parser, Element stageEl) throws InvalidXMLException {
        Element teleportEl = stageEl.getChild("teleport");
        if(teleportEl != null) {
            return new RandomPointProvider(parser.parse(teleportEl, new PointProviderAttributes()));
        } else {
            return null;
        }
    }
}
