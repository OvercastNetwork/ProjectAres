package tc.oc.pgm.kits;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.google.common.collect.Range;
import org.bukkit.inventory.ItemStack;
import org.jdom2.Element;
import tc.oc.commons.bukkit.inventory.ArmorType;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.pgm.blitz.LivesKit;
import tc.oc.pgm.compose.CompositionParser;
import tc.oc.pgm.doublejump.DoubleJumpKit;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.features.FeatureDefinitionParser;
import tc.oc.pgm.features.FeatureParser;
import tc.oc.pgm.features.MagicMethodFeatureParser;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.itemmeta.ItemModifier;
import tc.oc.pgm.map.MapLogger;
import tc.oc.pgm.physics.RelativeFlags;
import tc.oc.pgm.shield.ShieldKit;
import tc.oc.pgm.shield.ShieldParameters;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.utils.AllMaterialMatcher;
import tc.oc.pgm.utils.MethodParser;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.NodeSplitter;
import tc.oc.pgm.xml.property.PropertyBuilderFactory;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowFunction;
import static tc.oc.commons.core.stream.Collectors.toImmutableSet;

public class KitDefinitionParser extends MagicMethodFeatureParser<Kit> implements FeatureDefinitionParser<Kit> {

    @Inject protected FeatureDefinitionContext features;
    @Inject protected ItemParser itemParser;
    @Inject protected FilterParser filterParser;
    @Inject protected FeatureParser<TeamFactory> teamParser;
    @Inject protected CompositionParser<Kit> composer;
    @Inject protected KitParser kitParser;
    @Inject protected PropertyBuilderFactory<Boolean, ?> booleanParser;
    @Inject protected MapLogger mapLogger;
    @Inject protected ItemModifier itemModifier;

    private Stream<Kit> parseParentKits(Element el) throws InvalidXMLException {
        return Node.attributes(el, "parent", "parents")
                   .flatMap(attr -> NodeSplitter.LIST.split(attr)
                                                     .map(rethrowFunction(id -> kitParser.parseReference(attr, id.trim()))));
    }

    // TODO: this check can be removed after a little while
    private static final Set<String> LEGACY_ACTION_ATTRS = Stream.of(KitRule.Action.values())
                                                                 .map(action -> action.name().toLowerCase())
                                                                 .collect(toImmutableSet());
    @MethodParser
    Kit kit(Element el) throws InvalidXMLException {
        Node.nodes(el, LEGACY_ACTION_ATTRS).forEach(attr ->
            mapLogger.warning(attr, "Kit give/take/lend attributes are no longer supported")
        );

        return new KitNodeImpl(
            parseParentKits(el),
            composer.parseElement(el),
            filterParser.property(el, "filter").optional(StaticFilter.ALLOW),
            booleanParser.property(el, "force").elements(false).optional(), // attrs only, since <force> is a type of kit
            booleanParser.property(el, "potion-particles").optional()
        );
    }

    protected Kit makeItemKit(Optional<Slot.Player> slot, ItemStack item) throws InvalidXMLException {
        final ItemStack modded = itemModifier.modify(item).immutableCopy();
        return slot.map(s -> (Kit) new SlotItemKit(modded, s))
                   .orElseGet(() -> new FreeItemKit(modded));
    }

    public Kit parseArmorKit(Element el, ArmorType type) throws InvalidXMLException {
        return makeItemKit(Optional.of(Slot.Armor.forType(type)), itemParser.parseItem(el, true));
    }

    public Optional<Slot.Player> parseSlot(Element el) throws InvalidXMLException {
        return Optional.ofNullable(Node.fromAttr(el, "slot"))
                       .map(rethrowFunction(XMLUtils::parsePlayerSlot));
    }

    @MethodParser
    public Kit item(Element el) throws InvalidXMLException {
        return makeItemKit(parseSlot(el), itemParser.parseItem(el, true));
    }

    @MethodParser
    public Kit book(Element el) throws InvalidXMLException {
        return makeItemKit(parseSlot(el), itemParser.parseBook(el));
    }

    @MethodParser
    public Kit head(Element el) throws InvalidXMLException {
        return makeItemKit(parseSlot(el), itemParser.parseHead(el));
    }

    @MethodParser public Kit boots(Element el) throws InvalidXMLException { return parseArmorKit(el, ArmorType.BOOTS); }
    @MethodParser public Kit leggings(Element el) throws InvalidXMLException { return parseArmorKit(el, ArmorType.LEGGINGS); }
    @MethodParser public Kit chestplate(Element el) throws InvalidXMLException { return parseArmorKit(el, ArmorType.CHESTPLATE); }
    @MethodParser public Kit helmet(Element el) throws InvalidXMLException { return parseArmorKit(el, ArmorType.HELMET); }

    @MethodParser
    public Kit clear(Element el) throws InvalidXMLException {
        return new ClearKit(
            parseSlot(el),
            XMLUtils.parseMaterialMatcher(el, AllMaterialMatcher.INSTANCE)
        );
    }

    @MethodParser
    public Kit clear_items(Element el) throws InvalidXMLException {
        return new ClearItemsKit();
    }

    @MethodParser
    public Kit knockback_reduction(Element el) throws InvalidXMLException {
        return new KnockbackReductionKit(XMLUtils.parseNumber(el, Float.class));
    }

    @MethodParser
    public Kit walk_speed(Element el) throws InvalidXMLException {
        return new WalkSpeedKit(XMLUtils.parseNumber(el, Float.class, Range.closed(WalkSpeedKit.MIN, WalkSpeedKit.MAX)));
    }

    /*
      ~ <fly/>                      {FlyKit: allowFlight = true,  flying = null  }
      ~ <fly flying="false"/>       {FlyKit: allowFlight = true,  flying = false }
      ~ <fly allowFlight="false"/>  {FlyKit: allowFlight = false, flying = null  }
      ~ <fly flying="true"/>        {FlyKit: allowFlight = true,  flying = true  }
     */
    @MethodParser
    public Kit fly(Element el) throws InvalidXMLException {
        final boolean canFly = XMLUtils.parseBoolean(el.getAttribute("can-fly"), true);
        final Boolean flying = XMLUtils.parseBoolean(el.getAttribute("flying"), null);
        org.jdom2.Attribute flySpeedAtt = el.getAttribute("fly-speed");
        float flySpeedMultiplier = 1;
        if(flySpeedAtt != null) {
            flySpeedMultiplier = XMLUtils.parseNumber(el.getAttribute("fly-speed"), Float.class, Range.closed(FlyKit.MIN, FlyKit.MAX));
        }

        return new FlyKit(canFly, flying, flySpeedMultiplier);
    }

    @MethodParser({"effect", "potion"})
    public Kit effect(Element el) throws InvalidXMLException {
        return new PotionKit(itemParser.parsePotionEffect(el));
    }


    @MethodParser
    public Kit attribute(Element el) throws InvalidXMLException {
        return new AttributeKit(XMLUtils.parseAttributeModifier(el));
    }

    @MethodParser
    public Kit health(Element el) throws InvalidXMLException {
        int health = XMLUtils.parseNumber(el, Integer.class);
        if(health < 1 || health > 20) {
            throw new InvalidXMLException(health + " is not a valid health value, must be between 1 and 20", el);
        }
        return new HealthKit(health);
    }

    @MethodParser
    public Kit max_health(Element el) throws InvalidXMLException {
        return new MaxHealthKit(XMLUtils.parseNumber(el, Double.class, Range.atLeast(1d)));
    }

    @MethodParser
    public Kit saturation(Element el) throws InvalidXMLException {
        return new HungerKit(XMLUtils.parseNumber(el, Float.class, Range.atLeast(0f)), null);
    }

    @MethodParser
    public Kit foodlevel(Element el) throws InvalidXMLException {
        return new HungerKit(null, XMLUtils.parseNumber(el, Integer.class, Range.atLeast(0)));
    }

    @MethodParser
    public Kit double_jump(Element el) throws InvalidXMLException {
        return new DoubleJumpKit(XMLUtils.parseBoolean(el.getAttribute("enabled"), true),
                                 XMLUtils.parseNumber(el.getAttribute("power"), Float.class, DoubleJumpKit.DEFAULT_POWER),
                                 XMLUtils.parseDuration(el.getAttribute("recharge-time"), DoubleJumpKit.DEFAULT_RECHARGE),
                                 XMLUtils.parseBoolean(el.getAttribute("recharge-before-landing"), false));
    }

    @MethodParser
    public Kit reser_ender_pearls(Element el) throws InvalidXMLException {
        return new ResetEnderPearlsKit();
    }

    @MethodParser
    public Kit game_mode(Element el) throws InvalidXMLException {
        return new GameModeKit(XMLUtils.parseGameMode(new Node(el)));
    }

    @MethodParser
    public Kit shield(Element el) throws InvalidXMLException {
        return new ShieldKit(new ShieldParameters(XMLUtils.parseNumber(el.getAttribute("health"), Double.class, ShieldParameters.DEFAULT_HEALTH),
                                                  XMLUtils.parseDuration(el.getAttribute("delay"), ShieldParameters.DEFAULT_DELAY)));
    }

    @MethodParser
    public Kit fast_regeneration(Element el) throws InvalidXMLException {
        return new NaturalRegenerationKit(true, XMLUtils.parseBoolean(new Node(el)));
    }

    @MethodParser
    public Kit slow_regeneration(Element el) throws InvalidXMLException {
        return new NaturalRegenerationKit(false, XMLUtils.parseBoolean(new Node(el)));
    }

    @MethodParser
    public Kit hitbox(Element el) throws InvalidXMLException {
        return new HitboxKit(XMLUtils.parseNumber(Node.fromRequiredAttr(el, "width"), Double.class));
    }

    @MethodParser
    private Kit remove(Element el) throws InvalidXMLException {
        return features.validate(new RemoveKit(kitParser.parseReferenceElement(el)), new Node(el), RemovableValidation.get());
    }

    @MethodParser
    private Kit team_switch(Element el) throws InvalidXMLException {
        return new TeamSwitchKit(teamParser.property(el, "team").required());
    }

    @MethodParser
    private Kit eliminate(Element el) throws InvalidXMLException {
        return new EliminateKit();
    }

    private RelativeFlags parseRelativeFlags(Element el) throws InvalidXMLException {
        return RelativeFlags.of(XMLUtils.parseBoolean(el.getAttribute("yaw"), false),
                                XMLUtils.parseBoolean(el.getAttribute("pitch"), false));
    }

    @MethodParser
    private Kit impulse(Element el) throws InvalidXMLException {
        return new ImpulseKit(XMLUtils.parseVector(new Node(el)),
                              parseRelativeFlags(el));
    }

    @MethodParser
    private Kit force(Element el) throws InvalidXMLException {
        return new ForceKit(XMLUtils.parseVector(new Node(el)),
                            parseRelativeFlags(el));
    }

    @MethodParser
    private Kit lives(Element el) throws InvalidXMLException {
        return new LivesKit(XMLUtils.parseNumber(new Node(el), Integer.class, false, +1));
    }
}
