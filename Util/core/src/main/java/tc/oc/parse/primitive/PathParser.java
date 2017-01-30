package tc.oc.parse.primitive;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import tc.oc.parse.FormatException;
import tc.oc.parse.ParseException;
import tc.oc.parse.Parser;

public class PathParser implements Parser<Path> {
    @Override
    public Path parse(String text) throws ParseException {
        try {
            return Paths.get(text);
        } catch(InvalidPathException e) {
            throw new FormatException(e.getMessage());
        }
    }
}
