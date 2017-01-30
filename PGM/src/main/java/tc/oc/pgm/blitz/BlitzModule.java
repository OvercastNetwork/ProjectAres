package tc.oc.pgm.blitz;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.Range;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.pgm.ffa.FreeForAllModule;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.mutation.MutationMapModule;
import tc.oc.pgm.mutation.MutationMatchModule;
import tc.oc.pgm.teams.TeamModule;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

import static com.google.common.base.Preconditions.checkNotNull;


@ModuleDescription(name = "Blitz", follows = MutationMapModule.class)
public class BlitzModule implements MapModule, MatchModuleFactory<BlitzMatchModule> {
    final BlitzConfig config;

    public BlitzModule(BlitzConfig config) {
        this.config = checkNotNull(config);
    }

    @Override
    public Set<MapDoc.Gamemode> getGamemodes(MapModuleContext context) {
        return isEnabled() ? Collections.singleton(MapDoc.Gamemode.blitz) : Collections.emptySet();
    }

    @Override
    public BaseComponent getGameName(MapModuleContext context) {
        if(!isEnabled()) return null;
        if (context.hasModule(TeamModule.class)) {
            return new TranslatableComponent("match.scoreboard.playersRemaining.title");
        } else if (context.hasModule(FreeForAllModule.class) && config.getNumLives() > 1) {
            return new TranslatableComponent("match.scoreboard.livesRemaining.title");
        } else {
            return new TranslatableComponent("match.scoreboard.blitz.title");
        }
    }

    @Override
    public BlitzMatchModule createMatchModule(Match match) {
        return new BlitzMatchModule(match, this.config);
    }

    /**
     * In order to support {@link MutationMatchModule}, this module
     * will always create a {@link BlitzMatchModule}. However, if the lives are set to
     * {@link Integer#MAX_VALUE}, then it will fail to load on {@link BlitzMatchModule#shouldLoad()}.
     */
    public boolean isEnabled() {
        return config.lives != Integer.MAX_VALUE;
    }

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static BlitzModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        List<Element> blitzElements = doc.getRootElement().getChildren("blitz");
        BlitzConfig config = new BlitzConfig(Integer.MAX_VALUE, false);

        for(Element blitzEl : blitzElements) {
            boolean broadcastLives = XMLUtils.parseBoolean(blitzEl.getChild("broadcastLives"), true);
            int lives = XMLUtils.parseNumber(Node.fromChildOrAttr(blitzEl, "lives"), Integer.class, Range.atLeast(1), 1);
            config = new BlitzConfig(lives, broadcastLives);
        }

        return new BlitzModule(config);
    }
}
