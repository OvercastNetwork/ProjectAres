package tc.oc.parse.validate;

import tc.oc.parse.ValueException;

public interface Validation<T> {
    void validate(T value) throws ValueException;
}
