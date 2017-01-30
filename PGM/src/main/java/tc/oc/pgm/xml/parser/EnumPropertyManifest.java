package tc.oc.pgm.xml.parser;

import javax.annotation.Nullable;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.inject.TypeManifest;
import tc.oc.pgm.xml.property.PropertyManifest;

public class EnumPropertyManifest<T extends Enum<T>> extends TypeManifest<T> {

    protected EnumPropertyManifest() {
        this(null);
    }

    public EnumPropertyManifest(@Nullable TypeLiteral<T> type) {
        super(type);
    }

    @Override
    protected void configure() {
        install(new EnumParserManifest<>(type));
        install(new PropertyManifest<>(type));
    }
}
