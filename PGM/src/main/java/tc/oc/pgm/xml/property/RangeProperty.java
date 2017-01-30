package tc.oc.pgm.xml.property;

import javax.inject.Inject;

import com.google.common.collect.Range;
import com.google.inject.assistedinject.Assisted;
import org.jdom2.Element;
import tc.oc.pgm.xml.parser.RangeParser;

public class RangeProperty<T extends Comparable<T>> extends PropertyBuilder<Range<T>, RangeProperty<T>> {

    @Inject private RangeProperty(@Assisted Element parent, @Assisted String name, RangeParser<T> rangeParser) {
        super(parent, name, rangeParser);
    }
}
