package tc.oc.pgm.mutation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.commons.core.util.MapUtils;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

@ModuleDescription(name = "Mutation")
public class MutationMapModule implements MapModule, MatchModuleFactory<MutationMatchModule> {

    public static final double DEFAULT_WEIGHT = 1.0;
    public static final double DEFAULT_CHANCE = 0.05;
    public static final int DEFAULT_AMOUNT = 3;

    private final MutationOptions options;

    public MutationMapModule(MutationOptions options) {
        this.options = options;
    }

    @Override
    public MutationMatchModule createMatchModule(Match match) {
        return new MutationMatchModule(match, options);
    }

    public static MutationMapModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        Map<Mutation, Double> map = new HashMap<>();
        double chance = DEFAULT_CHANCE;
        int amount = DEFAULT_AMOUNT;
        // Get all the mutation weights defined in the XML
        for(Element elMutations : doc.getRootElement().getChildren("mutations")) {
            chance = XMLUtils.parseNumber(Node.fromChildOrAttr(elMutations, true, "chance"), Double.class, DEFAULT_CHANCE);
            amount = XMLUtils.parseNumber(Node.fromChildOrAttr(elMutations, true, "amount"), Integer.class, true, DEFAULT_AMOUNT);
            for (Element elMutation : elMutations.getChildren()) {
                map.put(XMLUtils.parseEnum(new Node(elMutation), elMutation.getName(), Mutation.class, "mutation"), XMLUtils.parseNumber(Node.fromChildOrAttr(elMutation, true, "weight"), Double.class, DEFAULT_WEIGHT));
            }
        }
        // Fill in any left-over mutations that may have been left out
        Set<Mutation> leftovers = new HashSet<>();
        for(Mutation mutation : Mutation.values()) {
            if(!map.containsKey(mutation)) leftovers.add(mutation);
        }
        MapUtils.putAll(map, leftovers, DEFAULT_WEIGHT);
        return new MutationMapModule(new MutationOptions(map, chance, amount));
    }
}
