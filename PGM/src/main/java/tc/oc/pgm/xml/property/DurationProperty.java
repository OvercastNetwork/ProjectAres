package tc.oc.pgm.xml.property;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import org.jdom2.Element;
import java.time.Duration;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.xml.parser.PrimitiveParser;

public class DurationProperty extends TransfiniteProperty<Duration, DurationProperty> {

    @Inject private DurationProperty(@Assisted Element parent, @Assisted String name, PrimitiveParser<Duration> parser) {
        super(parent, name, parser);
    }

    @Override
    protected boolean isFinite(Duration value) {
        return TimeUtils.isFinite(value);
    }
}
