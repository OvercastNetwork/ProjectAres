package tc.oc.parse.validate;

import java.nio.file.Path;

import tc.oc.parse.ValueException;

public class NormalizedPath implements Validation<Path> {
    @Override
    public void validate(Path value) throws ValueException {
        if(!value.equals(value.normalize())) {
            throw new ValueException("Path cannot contain . or ..");
        }
    }
}
