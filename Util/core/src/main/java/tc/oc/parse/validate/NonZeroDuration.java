package tc.oc.parse.validate;

import java.time.Duration;

import tc.oc.commons.core.util.Comparables;
import tc.oc.parse.ValueException;

public class NonZeroDuration implements Validation<Duration> {
    @Override
    public void validate(Duration value) throws ValueException {
        if(Comparables.lessOrEqual(value, Duration.ZERO)) {
            throw new ValueException("Time must be non-zero");
        }
    }
}
