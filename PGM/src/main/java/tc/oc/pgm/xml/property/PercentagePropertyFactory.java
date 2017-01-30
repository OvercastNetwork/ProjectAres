package tc.oc.pgm.xml.property;

import javax.inject.Inject;

import org.jdom2.Element;
import tc.oc.pgm.xml.parser.PercentageParser;

public class PercentagePropertyFactory implements PropertyBuilderFactory<Double, PercentagePropertyFactory.PercentageProperty> {

    private final PercentageParser percentageParser;

    @Inject private PercentagePropertyFactory(PercentageParser percentageParser) {
        this.percentageParser = percentageParser;
    }

    @Override
    public PercentageProperty property(Element parent, String name) {
        return new PercentageProperty(parent, name);
    }

    public class PercentageProperty extends ComparableProperty<Double, PercentageProperty> {
        PercentageProperty(Element parent, String name) {
            super(parent, name, percentageParser);
        }
    }
}
