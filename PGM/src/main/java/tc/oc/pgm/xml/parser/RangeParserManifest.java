package tc.oc.pgm.xml.parser;

import javax.annotation.Nullable;

import com.google.common.collect.Range;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.inject.KeyedManifest;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.commons.core.util.Ranges;
import tc.oc.pgm.xml.property.PropertyManifest;
import tc.oc.pgm.xml.property.RangeProperty;

public class RangeParserManifest<T extends Comparable<T>> extends KeyedManifest implements ParserBinders {

    private final TypeLiteral<T> type;
    private TypeArgument<T> typeArg;

    protected RangeParserManifest() {
        this(null);
    }

    public RangeParserManifest(@Nullable TypeLiteral<T> type) {
        this.type = type != null ? type : new ResolvableType<T>(){}.in(getClass());
        this.typeArg = new TypeArgument<T>(this.type){};
    }

    @Override
    protected Object manifestKey() {
        return type;
    }

    @Override
    protected void configure() {
        final TypeLiteral<Range<T>> rangeType = Ranges.typeOf(type);
        final TypeLiteral<RangeParser<T>> rangeParserType = new ResolvableType<RangeParser<T>>(){}.with(typeArg);
        final TypeLiteral<RangeProperty<T>> rangePropertyType = new ResolvableType<RangeProperty<T>>(){}.with(typeArg);

        bindPrimitiveParser(rangeType).to(rangeParserType); // NodeParser<Range<T>> -> RangeParser<T>
        bind(rangeParserType); // RangeParser<T>

        install(new PropertyManifest<>(rangeType, rangePropertyType));
    }
}
