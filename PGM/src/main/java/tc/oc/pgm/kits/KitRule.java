package tc.oc.pgm.kits;

import com.google.inject.ImplementedBy;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.features.FeatureValidationContext;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.FilterMatchModule;
import tc.oc.pgm.filters.parser.DynamicFilterValidation;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.NodeSplitter;
import tc.oc.pgm.xml.finder.Parent;

@FeatureInfo(name = "kit-rule", plural = "kits", singular = {"give", "take", "lend"})
@ImplementedBy(KitRuleImpl.class)
public interface KitRule extends FeatureDefinition {

    enum Action { GIVE, TAKE, LEND }

    @Property @Nodes(Parent.class) @Split(NodeSplitter.Name.class)
    Action action();

    @Property
    Kit kit();

    @Property
    @Validate(DynamicFilterValidation.class)
    Filter filter();
}

abstract class KitRuleImpl extends FeatureDefinition.Impl implements KitRule {
    @Override
    public void validate(FeatureValidationContext context) throws InvalidXMLException {
        if(action() == Action.TAKE || action() == Action.LEND) {
            context.validate(kit(), RemovableValidation.get());
        }
    }

    @Override
    public void load(Match match) {
        final FilterMatchModule fmm = match.needMatchModule(FilterMatchModule.class);
        switch(action()) {
            case GIVE:
                fmm.onRise(MatchPlayer.class, filter(), kit()::apply);
                break;

            case TAKE:
                fmm.onRise(MatchPlayer.class, filter(), kit()::remove);
                break;

            case LEND:
                fmm.onChange(MatchPlayer.class, filter(), (player, response) -> {
                    if(response) {
                        kit().apply(player);
                    } else {
                        kit().remove(player);
                    }
                });
                break;
        }
    }
}
