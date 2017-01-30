package tc.oc.pgm.xml.validate;

import java.time.Duration;
import javax.annotation.Nullable;

import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class DurationIs {
    public static class NotNegative implements Validation<Duration> {
        @Override
        public void validate(Duration value, @Nullable Node node) throws InvalidXMLException {
            if(value.isNegative()) {
                throw new InvalidXMLException("Time cannot be negative", node);
            }
        }
    }

    public static class NotZero implements Validation<Duration> {
        @Override
        public void validate(Duration value, @Nullable Node node) throws InvalidXMLException {
            if(value.isZero()) {
                throw new InvalidXMLException("Time cannot be zero", node);
            }
        }
    }

    public static class Positive implements Validation<Duration> {
        @Override
        public void validate(Duration value, @Nullable Node node) throws InvalidXMLException {
            if(value.isNegative() || value.isZero()) {
                throw new InvalidXMLException("Time must be greater than zero", node);
            }
        }
    }
}
