package tc.oc.pgm.xml.parser;

import com.google.inject.TypeLiteral;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.material.MaterialData;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;
import java.time.Duration;
import tc.oc.commons.bukkit.localization.MessageTemplate;
import tc.oc.commons.core.inject.SingletonManifest;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.NumberFactory;
import tc.oc.pgm.xml.property.DurationProperty;
import tc.oc.pgm.xml.property.MessageTemplateProperty;
import tc.oc.pgm.xml.property.NumberProperty;
import tc.oc.pgm.xml.property.PercentagePropertyFactory;
import tc.oc.pgm.xml.property.PropertyManifest;

/**
 * Configure {@link Parser}s
 */
public class ParserManifest extends SingletonManifest implements ParserBinders {

    @Override
    protected void configure() {
        NumberFactory.numberTypes().forEach(type -> bindNumber((Class) type));

        bindPrimitiveParser(Boolean.class).to(BooleanParser.class);
        bindPrimitiveParser(String.class).to(StringParser.class);
        bindPrimitiveParser(Duration.class).to(DurationParser.class);
        bindPrimitiveParser(ImVector.class).to(new TypeLiteral<VectorParser<Double>>(){});
        bindPrimitiveParser(Vector.class).to((TypeLiteral) new TypeLiteral<PrimitiveParser<ImVector>>(){});
        bindPrimitiveParser(Team.OptionStatus.class).to(TeamRelationParser.class);
        bindPrimitiveParser(MessageTemplate.class).to(MessageTemplateParser.class);
        bindPrimitiveParser(Material.class).to(MaterialParser.class);
        bindPrimitiveParser(MaterialData.class).to(MaterialDataParser.class);
        bindPrimitiveParser(Attribute.class).to(AttributeParser.class);

        bind(PercentageParser.class);
        bind(PercentagePropertyFactory.class);

        install(new EnumPropertyManifest<ChatColor>(){});
        install(new EnumPropertyManifest<EntityType>(){});
        install(new EnumPropertyManifest<DyeColor>(){});
        // etc...

        install(new PropertyManifest<>(Boolean.class));
        install(new PropertyManifest<>(String.class));
        install(new PropertyManifest<>(Duration.class, DurationProperty.class));
        install(new PropertyManifest<>(ImVector.class));
        install(new PropertyManifest<>(Vector.class));
        install(new PropertyManifest<>(MessageTemplate.class, MessageTemplateProperty.class));
    }

    private <T extends Number & Comparable<T>> void bindNumber(Class<T> rawType) {
        final TypeLiteral<T> type = TypeLiteral.get(rawType);
        final TypeArgument<T> typeArg = new TypeArgument<T>(type){};

        final TypeLiteral<NumberParser<T>> parserType = new ResolvableType<NumberParser<T>>(){}.with(typeArg);
        bind(parserType);
        bind(new ResolvableType<TransfiniteParser<T>>(){}.with(typeArg)).to(parserType);
        bind(new ResolvableType<PrimitiveParser<T>>(){}.with(typeArg)).to(parserType);
        bind(new ResolvableType<Parser<T>>(){}.with(typeArg)).to(parserType);

        final TypeLiteral<VectorParser<T>> vectorParserType = new ResolvableType<VectorParser<T>>(){}.with(typeArg);
        bind(vectorParserType);

        install(new PropertyManifest<>(type, new ResolvableType<NumberProperty<T>>(){}.with(typeArg)));

        if(Types.isAssignable(Comparable.class, type)) {
            install(new RangeParserManifest(type));
        }
    }
}
