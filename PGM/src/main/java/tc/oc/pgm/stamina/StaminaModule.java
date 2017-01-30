package tc.oc.pgm.stamina;

import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.stamina.mutators.SimpleMutator;
import tc.oc.pgm.stamina.mutators.StaminaMutator;
import tc.oc.pgm.stamina.symptoms.ArcherySymptom;
import tc.oc.pgm.stamina.symptoms.MeleeSymptom;
import tc.oc.pgm.stamina.symptoms.PotionSymptom;
import tc.oc.pgm.utils.NumericModifier;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

@ModuleDescription(name = "Stamina")
public class StaminaModule implements MapModule, MatchModuleFactory<StaminaMatchModule> {

    private final StaminaOptions options;

    public StaminaModule(StaminaOptions options) {
        this.options = options;
    }

    @Override
    public StaminaMatchModule createMatchModule(Match match) {
        return new StaminaMatchModule(match, options);
    }

    public static StaminaModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        StaminaOptions options = new StaminaOptions();
        boolean configured = false;
        for(Element elStamina : doc.getRootElement().getChildren("stamina")) {
            configured = true;
            parseOptions(context, elStamina, options);

        }
        return configured ? new StaminaModule(options) : null;
    }

    private static void parseOptions(MapModuleContext context, Element el, StaminaOptions options) throws InvalidXMLException {
        options.mutators.sneak = parseMutator(el, "sneak");
        options.mutators.stand = parseMutator(el, "stand");
        options.mutators.walk = parseMutator(el, "walk");
        options.mutators.run = parseMutator(el, "run");

        options.mutators.jump = parseMutator(el, "jump");
        options.mutators.runJump = parseMutator(el, "run-jump");
        options.mutators.injury = parseMutator(el, "injury");
        options.mutators.meleeMiss = parseMutator(el, "melee-miss");
        options.mutators.meleeHit = parseMutator(el, "melee-hit");
        options.mutators.archery = parseMutator(el, "archery");

        for(Element elSymptoms : XMLUtils.flattenElements(el, "symptoms")) {
            switch(elSymptoms.getName()) {
                case "potion":
                    options.symptoms.add(parsePotionSymptom(elSymptoms));
                    break;

                case "melee":
                    options.symptoms.add(parseMeleeSymptom(elSymptoms));
                    break;

                case "archery":
                    options.symptoms.add(parseArcherySymptom(elSymptoms));
                    break;

                default:
                    throw new InvalidXMLException("Invalid symptom type", elSymptoms);
            }
        }
    }

    private static StaminaMutator parseMutator(Element el, String name) throws InvalidXMLException {
        NumericModifier modifier = null;
        Element elMutators = el.getChild("mutators");
        if(elMutators != null) {
            modifier = XMLUtils.parseNumericModifier(elMutators.getChild(name), null);
        }
        return new SimpleMutator(name, modifier != null ? modifier : NumericModifier.ZERO, "stamina.mutator." + name);
    }

    private static PotionSymptom parsePotionSymptom(Element el) throws InvalidXMLException {
        return new PotionSymptom(XMLUtils.parseNumericRange(el, Double.class),
                                 XMLUtils.parsePotionEffectType(Node.fromRequiredAttr(el, "effect")),
                                 XMLUtils.parseNumber(Node.fromAttr(el, "amplifier"), Integer.class, 1) - 1);
    }

    private static MeleeSymptom parseMeleeSymptom(Element el) throws InvalidXMLException {
        return new MeleeSymptom(XMLUtils.parseNumericRange(el, Double.class),
                                XMLUtils.parseNumericModifier(el));
    }

    private static ArcherySymptom parseArcherySymptom(Element el) throws InvalidXMLException {
        return new ArcherySymptom(XMLUtils.parseNumericRange(el, Double.class),
                                  XMLUtils.parseNumericModifier(el));
    }
}
