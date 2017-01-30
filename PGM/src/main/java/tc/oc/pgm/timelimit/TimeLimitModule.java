package tc.oc.pgm.timelimit;

import java.time.Duration;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapModuleFactory;
import tc.oc.pgm.map.ProtoVersions;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.victory.DefaultResult;
import tc.oc.pgm.victory.MatchResult;
import tc.oc.pgm.victory.VictoryResultParser;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowFunction;

@ModuleDescription(name = "Time Limit")
public class TimeLimitModule implements MapModule, MatchModuleFactory<TimeLimitMatchModule> {
    private final @Nullable TimeLimitDefinition timeLimit;

    public TimeLimitModule(@Nullable TimeLimitDefinition limit) {
        this.timeLimit = limit;
    }

    @Override
    public TimeLimitMatchModule createMatchModule(Match match) {
        return new TimeLimitMatchModule(match, this.timeLimit);
    }

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static class Factory extends MapModuleFactory<TimeLimitModule> {

        @Inject private Provider<VictoryResultParser> victoryResultParser;

        @Override
        public TimeLimitModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
            TimeLimitDefinition timeLimit = parseTimeLimit(doc.getRootElement());
            timeLimit = parseLegacyTimeLimit(context, doc.getRootElement(), "score", timeLimit);
            timeLimit = parseLegacyTimeLimit(context, doc.getRootElement(), "blitz", timeLimit);

            // TimeLimitModule always loads
            return new TimeLimitModule(timeLimit);
        }

        private @Nullable TimeLimitDefinition parseLegacyTimeLimit(MapModuleContext context, Element el, String legacyTag, TimeLimitDefinition oldTimeLimit) throws InvalidXMLException {
            el = el.getChild(legacyTag);
            if(el != null) {
                TimeLimitDefinition newTimeLimit = parseTimeLimit(el);
                if(newTimeLimit != null) {
                    if(context.getProto().isNoOlderThan(ProtoVersions.REMOVE_SCORE_TIME_LIMIT)) {
                        throw new InvalidXMLException("<time> inside <" + legacyTag + "> is no longer supported, use root <time> instead", el);
                    }
                    if(oldTimeLimit != null) {
                        throw new InvalidXMLException("Time limit conflicts with another one that is already defined", el);
                    }
                    return newTimeLimit;
                }
            }

            return oldTimeLimit;
        }

        private @Nullable TimeLimitDefinition parseTimeLimit(Element el) throws InvalidXMLException {
            el = el.getChild("time");
            if(el == null) return null;

            final Duration duration = XMLUtils.parseDuration(Node.of(el));
            if(Comparables.greaterThan(duration, TimeLimit.MAX_DURATION)) {
                throw new InvalidXMLException("Time limit cannot exceed " + TimeLimit.MAX_DURATION.toDays() + " days", el);
            }

            return new TimeLimitDefinition(
                duration,
                parseVictoryCondition(Node.tryAttr(el, "result")),
                XMLUtils.parseBoolean(el.getAttribute("show"), true)
            );
        }

        private MatchResult parseVictoryCondition(Optional<Node> node) throws InvalidXMLException {
            return node.map(rethrowFunction(n -> victoryResultParser.get().parse(n)))
                       .orElseGet(DefaultResult::new);
        }
    }
}