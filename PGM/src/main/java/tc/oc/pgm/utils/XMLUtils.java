package tc.oc.pgm.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Splitter;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.gson.JsonParseException;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Skin;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.ItemAttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionBrew;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.registry.Key;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BlockVector;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;
import org.jdom2.Attribute;
import org.jdom2.Element;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.bukkit.localization.Translations;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.util.ArrayUtils;
import tc.oc.commons.core.util.NumberFactory;
import tc.oc.commons.core.util.Pair;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.xml.BoundedElement;
import tc.oc.pgm.xml.ElementFlattener;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.parser.BooleanParser;
import tc.oc.pgm.xml.parser.NumberParser;
import tc.oc.pgm.xml.parser.PrimitiveParser;
import tc.oc.pgm.xml.parser.StringParser;
import tc.oc.pgm.xml.parser.TeamRelationParser;
import tc.oc.pgm.xml.parser.VectorParser;
import tc.oc.pgm.xml.property.DurationProperty;
import tc.oc.pgm.xml.property.NumberProperty;
import tc.oc.pgm.xml.property.PropertyBuilder;
import tc.oc.pgm.xml.property.PropertyBuilderFactory;

public class XMLUtils {

    @Inject private static PropertyBuilderFactory<Duration, DurationProperty> durationFactory;
    @Inject private static PrimitiveParser<org.bukkit.attribute.Attribute> attributeParser;

    /**
     * @param parentTagNames       Names of elements deeper than the root that can be traversed
     * @param childTagNames        Names of elements that can be included in the result
     * @param minChildDepth        Minimum depth of elements that can be in the result, relative to the children of root
     */
    public static List<Element> flattenElements(Element root, Set<String> parentTagNames, @Nullable Set<String> childTagNames, int minChildDepth) {
        return new ElementFlattener(parentTagNames, childTagNames, minChildDepth)
            .flattenChildren(root)
            .collect(Collectors.toList());
    }

    public static List<Element> flattenElements(Element root, Set<String> parentTagNames, @Nullable Set<String> childTagNames) {
        return flattenElements(root, parentTagNames, childTagNames, 1);
    }

    public static List<Element> flattenElements(Element root, Set<String> parentTagNames) {
        return flattenElements(root, parentTagNames, null);
    }

    public static List<Element> flattenElements(Element root, String parentTagName, @Nullable String childTagName, int minChildDepth) {
        return flattenElements(root, ImmutableSet.of(parentTagName), childTagName == null ? null : ImmutableSet.of(childTagName), minChildDepth);
    }

    public static List<Element> flattenElements(Element root, String parentTagName, @Nullable String childTagName) {
        return flattenElements(root, parentTagName, childTagName, 1);
    }

    public static List<Element> flattenElements(Element root, String parentTagName) {
        return flattenElements(root, parentTagName, null);
    }

    public static Iterable<Element> getChildren(Element parent, Collection<String> names) {
        return Iterables.filter(parent.getChildren(), el -> names.contains(el.getName()));
    }

    public static Iterable<Element> getChildren(Element parent, String...names) {
        return getChildren(parent, Arrays.asList(names));
    }

    public static @Nullable Attribute getAttribute(Element parent, Collection<String> names) {
        for(String name : names) {
            final Attribute attr = parent.getAttribute(name);
            if(attr != null) return attr;
        }
        return null;
    }

    public static @Nullable Attribute getAttribute(Element parent, String...names) {
        return getAttribute(parent, Arrays.asList(names));
    }

    public static Iterable<Attribute> getAttributes(Element parent, Collection<String> names) {
        return Iterables.filter(parent.getAttributes(), attr -> names.contains(attr.getName()));
    }

    public static Iterable<Attribute> getAttributes(Element parent, String...names) {
        return getAttributes(parent, Arrays.asList(names));
    }

    public static @Nullable Element getUniqueChild(Element parent, String name, String...aliases) throws InvalidXMLException {
        return getUniqueChild(parent, name, ImmutableSet.copyOf(aliases));
    }

    public static @Nullable Element getUniqueChild(Element parent, String name, Set<String> aliases) throws InvalidXMLException {
        List<Element> children = new ArrayList<>();
        for(String alias : Sets.union(ImmutableSet.of(name), aliases)) {
            children.addAll(parent.getChildren(alias));
        }

        if(children.size() > 1) {
            throw new InvalidXMLException("multiple '" + name + "' tags not allowed", parent);
        }
        return children.isEmpty() ? null : children.get(0);
    }

    public static Element getRequiredUniqueChild(Element parent, String name, String...aliases) throws InvalidXMLException {
        aliases = ArrayUtils.append(aliases, name);

        List<Element> children = new ArrayList<>();
        for(String alias : aliases) {
            children.addAll(parent.getChildren(alias));
        }

        if(children.size() > 1) {
            throw new InvalidXMLException("multiple '" + name + "' tags not allowed", parent);
        } else if (children.isEmpty()) {
            throw new InvalidXMLException("child tag '" + name + "' is required", parent);
        }
        return children.get(0);
    }

    public static Element getRequiredUniqueChild(Element parent) throws InvalidXMLException {
        if(parent.getChildren().size() > 1) {
            throw new InvalidXMLException("multiple elements not allowed", parent);
        } else if (parent.getChildren().isEmpty()) {
            throw new InvalidXMLException("child element is required", parent);
        }
        return parent.getChildren().get(0);
    }

    public static Attribute getRequiredAttribute(Element el, String name, String...aliases) throws InvalidXMLException {
        aliases = ArrayUtils.append(aliases, name);

        Attribute attr = null;
        for(String alias : aliases) {
            Attribute a = el.getAttribute(alias);
            if(a != null) {
                if(attr == null) {
                    attr = a;
                } else {
                    throw new InvalidXMLException("attributes '" + attr.getName() + "' and '" + alias + "' are aliases for the same thing, and cannot be combined", el);
                }
            }
        }

        if(attr == null) {
            throw new InvalidXMLException("attribute '" + name + "' is required", el);
        }

        return attr;
    }

    public static PropertyBuilder<Boolean, ?> parseBoolean(Element parent, String name) {
        return new PropertyBuilder<>(parent, name, BooleanParser.get());
    }

    private static Boolean parseBoolean(Node node, String value) throws InvalidXMLException {
        return BooleanParser.get().parse(node, value);
    }

    public static Boolean parseBoolean(Node node) throws InvalidXMLException {
        return node == null ? null : parseBoolean(node, node.getValue());
    }

    public static Boolean parseBoolean(@Nullable Node node, Boolean def) throws InvalidXMLException {
        return node == null ? def : parseBoolean(node);
    }

    public static Boolean parseBoolean(@Nullable Element el, Boolean def) throws InvalidXMLException {
        return el == null ? def : parseBoolean(new Node(el));
    }

    public static Boolean parseBoolean(@Nullable Attribute attr, Boolean def) throws InvalidXMLException {
        return attr == null ? def : parseBoolean(new Node(attr));
    }

    public static Optional<Boolean> parseOptionalBoolean(@Nullable Node node) throws InvalidXMLException {
        return node == null ? Optional.empty() : Optional.of(parseBoolean(node));
    }

    public static <T extends Number & Comparable<T>> NumberProperty<T> parseNumber(Element parent, String name, Class<T> type) {
        return new NumberProperty<>(parent, name, type);
    }

    public static <T extends Number> T parseNumber(Node node, String text, Class<T> type, boolean infinity) throws InvalidXMLException {
        final T value = NumberParser.get(type).parse(node, text);
        if(!infinity && !NumberFactory.get(type).isFinite(value)) {
            throw new InvalidXMLException("Number must be finite", node);
        }
        return value;
    }

    public static <T extends Number> Optional<T> parseOptionalNumber(@Nullable Node node, Class<T> type) throws InvalidXMLException {
        return Optional.ofNullable(parseNumber(node, type, null));
    }

    public static <T extends Number> T parseNumber(Node node, String text, Class<T> type, boolean infinity, T def) throws InvalidXMLException {
        return node == null ? def : parseNumber(node, text, type, infinity);
    }

    public static <T extends Number> T parseNumber(Node node, String text, Class<T> type, T def) throws InvalidXMLException {
        return parseNumber(node, text, type, false, def);
    }

    public static <T extends Number> T parseNumber(Node node, String text, Class<T> type) throws InvalidXMLException {
        return parseNumber(node, text, type, false);
    }

    public static <T extends Number> T parseNumber(Node node, Class<T> type, boolean infinity) throws InvalidXMLException {
        return parseNumber(node, node.getValue(), type, infinity);
    }

    public static <T extends Number> T parseNumber(Node node, Class<T> type) throws InvalidXMLException {
        return parseNumber(node, node.getValue(), type);
    }

    public static <T extends Number> T parseNumber(Attribute attr, Class<T> type) throws InvalidXMLException {
        return parseNumber(new Node(attr), type);
    }

    public static <T extends Number> T parseNumber(Element el, Class<T> type) throws InvalidXMLException {
        return parseNumber(new Node(el), type);
    }

    public static <T extends Number> T parseNumber(Node node, Class<T> type, boolean infinity, T def) throws InvalidXMLException {
        if(node == null) {
            return def;
        } else {
            return parseNumber(node, node.getValue(), type, infinity);
        }
    }

    public static <T extends Number> T parseNumber(Node node, Class<T> type, T def) throws InvalidXMLException {
        return parseNumber(node, type, false, def);
    }

    public static <T extends Number> T parseNumber(Element el, Class<T> type, T def) throws InvalidXMLException {
        if(el == null) {
            return def;
        } else {
            return parseNumber(el, type);
        }
    }

    public static <T extends Number> T parseNumber(Attribute attr, Class<T> type, T def) throws InvalidXMLException {
        if(attr == null) {
            return def;
        } else {
            return parseNumber(attr, type);
        }
    }

    // Note: all the range overloads allow infinities if they are within the range

    public static <T extends Number & Comparable<T>> T parseNumber(Element el, Class<T> type, Range<T> range) throws InvalidXMLException {
        return parseNumber(new Node(el), type, range);
    }

    public static <T extends Number & Comparable<T>> T parseNumber(Attribute attr, Class<T> type, Range<T> range) throws InvalidXMLException {
        return parseNumber(new Node(attr), type, range);
    }

    public static <T extends Number & Comparable<T>> T parseNumber(Node node, Class<T> type, Range<T> range) throws InvalidXMLException {
        return parseNumber(node, node.getValueNormalize(), type, range);
    }

    public static <T extends Number & Comparable<T>> T parseNumber(Node node, String text, Class<T> type, Range<T> range) throws InvalidXMLException {
        T value = parseNumber(node, text, type, true);
        if(!range.contains(value)) {
            throw new InvalidXMLException(value + " is not in the range " + range, node);
        }
        return value;
    }

    public static <T extends Number & Comparable<T>> T parseNumber(Node node, String text, Class<T> type, Range<T> range, T def) throws InvalidXMLException {
        return node == null ? def : parseNumber(node, text, type, range);
    }

    public static <T extends Number & Comparable<T>> T parseNumber(Node node, Class<T> type, Range<T> range, T def) throws InvalidXMLException {
        return node == null ? def : parseNumber(node, type, range);
    }

    private static final Pattern RANGE_RE = Pattern.compile("\\s*(\\(|\\[)\\s*([^,]+)\\s*,\\s*([^\\)\\]]+)\\s*(\\)|\\])\\s*");

    /**
     * Parse a range in the standard mathematical format e.g.
     *
     *     [0, 1) for a closed-open range from 0 to 1.
     *
     * Can also parse single numbers as a closed range e.g.
     *
     *     5 for a closed-closed range from 5 to 5.
     */
    public static <T extends Number & Comparable<T>> Range<T> parseNumericRange(Node node, Class<T> type) throws InvalidXMLException {
        String value = node.getValue();

        Matcher matcher = RANGE_RE.matcher(value);
        if(!matcher.matches()) {
            T number = parseNumber(node, value, type, (T) null);
            if(number != null) {
                return Range.singleton(number);
            }
            throw new InvalidXMLException("Invalid " + type.getSimpleName().toLowerCase() + " range '" + value + "'", node);
        }

        T lower = parseNumber(node, matcher.group(2), type, true);
        T upper = parseNumber(node, matcher.group(3), type, true);

        BoundType lowerType = null, upperType = null;
        if(!Double.isInfinite(lower.doubleValue())) {
            lowerType = "(".equals(matcher.group(1)) ? BoundType.OPEN : BoundType.CLOSED;
        }
        if(!Double.isInfinite(upper.doubleValue())) {
            upperType = ")".equals(matcher.group(4)) ? BoundType.OPEN : BoundType.CLOSED;
        }

        if(lower.compareTo(upper) == 1) {
            throw new InvalidXMLException("range lower bound (" + lower + ") cannot be greater than upper bound (" + upper + ")", node);
        }

        if(lowerType == null) {
            if(upperType == null) {
                return Range.all();
            } else {
                return Range.upTo(upper, upperType);
            }
        } else {
            if(upperType == null) {
                return Range.downTo(lower, lowerType);
            } else {
                return Range.range(lower, lowerType, upper, upperType);
            }
        }
    }

    /**
     * Parse a numeric range from attributes on the given element specifying the bounds of the range, specifically:
     *
     *      gt gte lt lte
     */
    public static <T extends Number & Comparable<T>> Range<T> parseNumericRange(Element el, Class<T> type) throws InvalidXMLException {
        return parseNumericRange(el, type, Range.all());
    }

    public static <T extends Number & Comparable<T>> Range<T> parseNumericRange(Element el, Class<T> type, Range<T> def) throws InvalidXMLException {
        Attribute lt = el.getAttribute("lt");
        Attribute lte = getAttribute(el, "lte", "max");
        Attribute gt = el.getAttribute("gt");
        Attribute gte = getAttribute(el, "gte", "min");

        if(lt != null && lte != null) throw new InvalidXMLException("Conflicting upper bound for numeric range", el);
        if(gt != null && gte != null) throw new InvalidXMLException("Conflicting lower bound for numeric range", el);

        BoundType lowerBoundType, upperBoundType;
        T lowerBound, upperBound;

        if(gt != null) {
            lowerBound = parseNumber(gt, type, (T) null);
            lowerBoundType = BoundType.OPEN;
        } else {
            lowerBound = parseNumber(gte, type, (T) null);
            lowerBoundType = BoundType.CLOSED;
        }

        if(lt != null) {
            upperBound = parseNumber(lt, type, (T) null);
            upperBoundType = BoundType.OPEN;
        } else {
            upperBound = parseNumber(lte, type, (T) null);
            upperBoundType = BoundType.CLOSED;
        }

        if(lowerBound == null) {
            if(upperBound == null) {
                return def;
            } else {
                return Range.upTo(upperBound, upperBoundType);
            }
        } else {
            if(upperBound == null) {
                return Range.downTo(lowerBound, lowerBoundType);
            } else {
                return Range.range(lowerBound, lowerBoundType, upperBound, upperBoundType);
            }
        }
    }

    public static DurationProperty parseDuration(Element parent, String name) {
        return durationFactory.property(parent, name);
    }

    public static Duration parseDuration(@Nullable Node node, Duration def) throws InvalidXMLException {
        if(node == null) return def;
        try {
            return TimeUtils.parseDuration(node.getValueNormalize());
        } catch(DateTimeParseException e) {
            throw new InvalidXMLException("Invalid time format", node, e);
        }
    }

    public static @Nullable Duration parseDuration(Node node) throws InvalidXMLException {
        return parseDuration(node, null);
    }

    public static Duration parseDuration(Element el, Duration def) throws InvalidXMLException {
        return parseDuration(Node.fromNullable(el), def);
    }

    public static Duration parseDuration(Attribute attr, Duration def) throws InvalidXMLException {
        return parseDuration(Node.fromNullable(attr), def);
    }

    public static Duration parseDuration(Attribute attr) throws InvalidXMLException {
        return parseDuration(attr, null);
    }

    public static Duration parseTickDuration(Node node, String text) throws InvalidXMLException {
        if("oo".equals(text)) return TimeUtils.INF_POSITIVE;
        try {
            return Duration.ofMillis(Integer.parseInt(text) * 50);
        } catch(NumberFormatException e) {
            return parseDuration(node);
        }
    }

    public static Duration parseTickDuration(Node node) throws InvalidXMLException {
        return parseTickDuration(node, node.getValueNormalize());
    }

    public static Duration parseTickDuration(Node node, Duration def) throws InvalidXMLException {
        return node == null ? def : parseDuration(node);
    }

    public static Duration parseSecondDuration(Node node, String text) throws InvalidXMLException {
        if("oo".equals(text)) return TimeUtils.INF_POSITIVE;
        try {
            return Duration.ofSeconds(Integer.parseInt(text));
        } catch(NumberFormatException e) {
            return parseDuration(node);
        }
    }

    public static Duration parseSecondDuration(Node node) throws InvalidXMLException {
        return parseSecondDuration(node, node.getValueNormalize());
    }

    public static Duration parseSecondDuration(Node node, Duration def) throws InvalidXMLException {
        return node == null ? def : parseSecondDuration(node);
    }

    public static Key parseKey(Node node, String text) throws InvalidXMLException {
        return Bukkit.key(text);
    }

    public static Key parseKey(Node node) throws InvalidXMLException {
        return parseKey(node, node.getValueNormalize());
    }

    public static Key parseKey(Node node, Key def) throws InvalidXMLException {
        return node == null ? def : parseKey(node);
    }

    public static Class<? extends Entity> parseEntityType(Element el) throws InvalidXMLException {
        return parseEntityType(new Node(el));
    }

    public static Class<? extends Entity> parseEntityTypeAttribute(Element el, String attributeName, Class<? extends Entity> def) throws InvalidXMLException {
        Node node = Node.fromAttr(el, attributeName);
        return node == null ? def : parseEntityType(node);
    }

    public static Class<? extends Entity> parseEntityType(Node node) throws InvalidXMLException {
        return parseEntityType(node, node.getValue());
    }

    public static Class<? extends Entity> parseEntityType(Node node, String value) throws InvalidXMLException {
        if(!value.matches("[a-zA-Z0-9_]+")) {
            throw new InvalidXMLException("Invalid entity type '" + value + "'", node);
        }

        try {
            return Class.forName("org.bukkit.entity." + value).asSubclass(Entity.class);
        }
        catch(ClassNotFoundException | ClassCastException e) {
            throw new InvalidXMLException("Invalid entity type '" + value + "'", node);
        }
    }

    public static <T extends Number> PropertyBuilder<ImVector, ?> parseVector(Element el, String name, Class<T> type) {
        return new PropertyBuilder<>(el, name, VectorParser.get(type));
    }

    public static <T extends Number> PropertyBuilder<ImVector, ?> parseVector(Element el, String name) {
        return parseVector(el, name, Double.class);
    }

    public static Vector parseVector(Node node, String value) throws InvalidXMLException {
        if(node == null) return null;

        String[] components = value.trim().split("\\s*,\\s*");
        if(components.length != 3) {
            throw new InvalidXMLException("Invalid vector format", node);
        }
        try {
            return new Vector(parseNumber(node, components[0], Double.class, true),
                              parseNumber(node, components[1], Double.class, true),
                              parseNumber(node, components[2], Double.class, true));
        }
        catch(NumberFormatException e) {
            throw new InvalidXMLException("Invalid vector format", node);
        }
    }

    public static Vector parseVector(Node node) throws InvalidXMLException {
        return node == null ? null : parseVector(node, node.getValue());
    }

    public static Vector parseVector(Node node, Vector def) throws InvalidXMLException {
        return node == null ? def : parseVector(node);
    }

    public static Vector parseVector(Attribute attr, String value) throws InvalidXMLException {
        return attr == null ? null : parseVector(new Node(attr), value);
    }

    public static Vector parseVector(Attribute attr) throws InvalidXMLException {
        return attr == null ? null : parseVector(attr, attr.getValue());
    }

    public static Vector parseVector(Attribute attr, Vector def) throws InvalidXMLException {
        return attr == null ? def : parseVector(attr);
    }

    public static Vector parse2DVector(Node node, String value) throws InvalidXMLException {
        String[] components = value.trim().split("\\s*,\\s*");
        if(components.length != 2) {
            throw new InvalidXMLException("Invalid 2D vector format", node);
        }
        try {
            return new Vector(parseNumber(node, components[0], Double.class, true),
                              0d,
                              parseNumber(node, components[1], Double.class, true));
        }
        catch(NumberFormatException e) {
            throw new InvalidXMLException("Invalid 2D vector format", node);
        }
    }

    public static Vector parse2DVector(Node node) throws InvalidXMLException {
        return parse2DVector(node, node.getValue());
    }

    public static Vector parse2DVector(Node node, Vector def) throws InvalidXMLException {
        return node == null ? def : parse2DVector(node);
    }

    public static BlockVector parseBlockVector(Node node, BlockVector def) throws InvalidXMLException {
        if(node == null) return def;

        String[] components = node.getValue().trim().split("\\s*,\\s*");
        if(components.length != 3) {
            throw new InvalidXMLException("Invalid block location", node);
        }
        try {
            return new BlockVector(Integer.parseInt(components[0]),
                                   Integer.parseInt(components[1]),
                                   Integer.parseInt(components[2]));
        }
        catch(NumberFormatException e) {
            throw new InvalidXMLException("Invalid block location", node);
        }
    }

    public static BlockVector parseBlockVector(Node node) throws InvalidXMLException {
        return parseBlockVector(node, null);
    }

    public static NumericModifier parseNumericModifier(Element el) throws InvalidXMLException {
        return new NumericModifier(parseNumber(el.getAttribute("add"), Double.class, 0d),
                                   parseNumber(el.getAttribute("mul"), Double.class, 0d));
    }

    public static NumericModifier parseNumericModifier(@Nullable Element el, NumericModifier def) throws InvalidXMLException {
        return el == null ? def : parseNumericModifier(el);
    }

    public static DyeColor parseDyeColor(Attribute attr) throws InvalidXMLException {
        String name = attr.getValue().replace(" ", "_").toUpperCase();
        try {
            return DyeColor.valueOf(name);
        }
        catch(IllegalArgumentException e) {
            throw new InvalidXMLException("Invalid dye color '" + attr.getValue() + "'", attr);
        }
    }

    public static DyeColor parseDyeColor(Attribute attr, DyeColor def) throws InvalidXMLException {
        return attr == null ? def : parseDyeColor(attr);
    }

    public static Material parseMaterial(Node node, String text) throws InvalidXMLException {
        Material material = Material.matchMaterial(text);
        if(material == null) {
            throw new InvalidXMLException("Unknown material '" + text + "'", node);
        }
        return material;
    }

    public static Material parseMaterial(Node node) throws InvalidXMLException {
        return parseMaterial(node, node.getValueNormalize());
    }

    public static Material parseBlockMaterial(Node node, String text) throws InvalidXMLException {
        Material material = parseMaterial(node, text);
        if(!material.isBlock()) {
            throw new InvalidXMLException("Material " + material.name() + " is not a block", node);
        }
        return material;
    }

    public static Material parseBlockMaterial(Node node) throws InvalidXMLException {
        return node == null ? null : parseBlockMaterial(node, node.getValueNormalize());
    }

    public static MaterialData parseMaterialData(Node node, String text) throws InvalidXMLException {
        String[] pieces = text.split(":");
        Material material = parseMaterial(node, pieces[0]);
        byte data;
        if(pieces.length > 1) {
            data = parseNumber(node, pieces[1], Byte.class);
        } else {
            data = 0;
        }
        return material.getNewData(data);
    }

    public static MaterialData parseMaterialData(Node node, MaterialData def) throws InvalidXMLException {
        return node == null ? def : parseMaterialData(node, node.getValueNormalize());
    }

    public static MaterialData parseMaterialData(Node node) throws InvalidXMLException {
        return parseMaterialData(node, (MaterialData) null);
    }

    public static MaterialData parseBlockMaterialData(Node node, String text) throws InvalidXMLException {
        if(node == null) return null;
        MaterialData material = parseMaterialData(node, text);
        if(!material.getItemType().isBlock()) {
            throw new InvalidXMLException("Material " + material.getItemType().name() + " is not a block", node);
        }
        return material;
    }

    public static MaterialData parseBlockMaterialData(Node node, MaterialData def) throws InvalidXMLException {
        return node == null ? def : parseBlockMaterialData(node, node.getValueNormalize());
    }

    public static MaterialData parseBlockMaterialData(Node node) throws InvalidXMLException {
        return parseBlockMaterialData(node, (MaterialData) null);
    }

    public static MaterialPattern parseMaterialPattern(Node node, String value) throws InvalidXMLException {
        try {
            return MaterialPattern.parse(value);
        }
        catch(IllegalArgumentException e) {
            throw new InvalidXMLException(e.getMessage(), node);
        }
    }

    public static MaterialPattern parseMaterialPattern(Node node) throws InvalidXMLException {
        return parseMaterialPattern(node, node.getValue());
    }

    public static MaterialPattern parseMaterialPattern(Node node, MaterialPattern def) throws InvalidXMLException {
        return node == null ? def : parseMaterialPattern(node);
    }

    public static MaterialPattern parseMaterialPattern(Element el) throws InvalidXMLException {
        return parseMaterialPattern(new Node(el));
    }

    public static MaterialPattern parseMaterialPattern(Attribute attr) throws InvalidXMLException {
        return parseMaterialPattern(new Node(attr));
    }

    public static ImmutableSet<MaterialPattern> parseMaterialPatternSet(Node node) throws InvalidXMLException {
        ImmutableSet.Builder<MaterialPattern> patterns = ImmutableSet.builder();
        for(String value : Splitter.on(";").split(node.getValue())) {
            patterns.add(parseMaterialPattern(node, value));
        }
        return patterns.build();
    }

    public static MaterialMatcher parseMaterialMatcher(Element el) throws InvalidXMLException {
        return parseMaterialMatcher(el, NoMaterialMatcher.INSTANCE);
    }

    public static MaterialMatcher parseMaterialMatcher(Element el, MaterialMatcher empty) throws InvalidXMLException {
        Set<MaterialMatcher> matchers = new HashSet<>();

        final Attribute attrMaterial = el.getAttribute("material");
        if(attrMaterial != null) {
            matchers.add(parseMaterialPattern(attrMaterial));
        }

        for(Element elChild : el.getChildren()) {
            switch(elChild.getName()) {
                case "all-materials":
                case "all-items":
                    return AllMaterialMatcher.INSTANCE;

                case "all-blocks":
                    matchers.add(BlockMaterialMatcher.INSTANCE);
                    break;

                case "material":
                case "item":
                    matchers.add(parseMaterialPattern(elChild));
                    break;

                default:
                    throw new InvalidXMLException("Unknown material matcher tag", elChild);
            }
        }

        return CompoundMaterialMatcher.of(matchers, empty);
    }

    public static PotionEffectType parsePotionEffectType(Node node) throws InvalidXMLException {
        return parsePotionEffectType(node, node.getValue());
    }

    public static PotionEffectType parsePotionEffectType(Node node, String text) throws InvalidXMLException {
        PotionEffectType type = PotionEffectType.getByName(text.toUpperCase().replace(" ", "_"));
        if(type == null) type = Bukkit.potionEffectRegistry().get(parseKey(node, text));
        if(type == null) {
            throw new InvalidXMLException("Unknown potion effect '" + node.getValue() + "'", node);
        }
        return type;
    }

    private static PotionEffect createPotionEffect(Node node, PotionEffectType type, Duration duration, int amplifier, boolean ambient) throws InvalidXMLException {
        if(PotionEffectType.HEALTH_BOOST.equals(type) && amplifier <= -6) {
            // HB -5 kills the player instantly and sets their max health to 0,
            // and we don't seem to be able to change it before the vanilla death screen shows.
            throw new InvalidXMLException("Health boost level -5 and below is not supported", node);
        }
        return new PotionEffect(type,
                                TimeUtils.isInfPositive(duration) ? Integer.MAX_VALUE : (int) (duration.toMillis() / 50),
                                Duration.ZERO.equals(duration) ? 0 : amplifier, // negative amplifier may cause an error, but we can ignore it if duration is zero
                                ambient);
    }

    public static PotionEffect parsePotionEffect(Element el) throws InvalidXMLException {
        Node node = new Node(el);
        PotionEffectType type = parsePotionEffectType(node);
        Duration duration = parseSecondDuration(Node.fromAttr(el, "duration"), TimeUtils.INF_POSITIVE);
        int amplifier = parseNumber(Node.fromAttr(el, "amplifier"), Integer.class, 1) - 1;
        boolean ambient = parseBoolean(Node.fromAttr(el, "ambient"), false);

        return createPotionEffect(node, type, duration, amplifier, ambient);
    }

    public static PotionEffect parseCompactPotionEffect(Node node, String text) throws InvalidXMLException {
        String[] parts = text.split(":");

        if(parts.length == 0) throw new InvalidXMLException("Missing potion effect", node);
        PotionEffectType type = parsePotionEffectType(node, parts[0]);
        Duration duration = TimeUtils.INF_POSITIVE;
        int amplifier = 0;
        boolean ambient = false;

        if(parts.length >= 2) {
            duration = parseTickDuration(node, parts[1]);
            if(parts.length >= 3) {
                amplifier = parseNumber(node, parts[2], Integer.class);
                if(parts.length >= 4) {
                    ambient = parseBoolean(node, parts[3]);
                }
            }
        }

        return createPotionEffect(node, type, duration, amplifier, ambient);
    }

    public static PotionBrew parsePotion(Node node, String text) throws InvalidXMLException {
        final PotionBrew brew = Bukkit.potionRegistry().get(parseKey(node, text));
        if(brew == null) {
            throw new InvalidXMLException("Unknown potion '" + text + "'", node);
        }
        return brew;
    }

    public static PotionBrew parsePotion(Node node) throws InvalidXMLException {
        return parsePotion(node, node.getValueNormalize());
    }

    public static PotionBrew parsePotion(Node node, PotionBrew def) throws InvalidXMLException {
        return node == null ? def : parsePotion(node);
    }

    public static PotionBrew parsePotionOrFallback(Node node) throws InvalidXMLException {
        final PotionBrew brew = parsePotion(node, (PotionBrew) null);
        return brew != null ? brew : Bukkit.potionRegistry().getFallback();
    }

    public static <T extends Enum<T>> T parseEnum(Node node, String text, Class<T> type, String readableType) throws InvalidXMLException {
        text = text.trim().replace(' ', '_');
        try {
            // First, try the fast way
            return Enum.valueOf(type, text);
        } catch(IllegalArgumentException ex) {
            // If that fails, search for a case-insensitive match, without assuming enums are always uppercase
            for(T value : type.getEnumConstants()) {
                if(value.name().equalsIgnoreCase(text)) return value;
            }
            throw new InvalidXMLException("Unknown " + readableType + " '" + text + "'", node);
        }
    }

    public static <T extends Enum<T>> T parseEnum(@Nullable Node node, Class<T> type, String readableType, @Nullable T def) throws InvalidXMLException {
        if(node == null) return def;
        return parseEnum(node, node.getValueNormalize(), type, readableType);
    }

    public static <T extends Enum<T>> T parseEnum(@Nullable Node node, Class<T> type, String readableType) throws InvalidXMLException {
        return parseEnum(node, type, readableType, null);
    }

    public static <T extends Enum<T>> T parseEnum(Element el, Class<T> type) throws InvalidXMLException {
        return parseEnum(new Node(el), type, type.getSimpleName());
    }

    public static <T extends Enum<T>> T parseEnum(Element el, Class<T> type, String readableType) throws InvalidXMLException {
        return parseEnum(new Node(el), type, readableType);
    }

    public static <T extends Enum<T>> T parseEnum(Attribute attr, Class<T> type, String readableType) throws InvalidXMLException {
        return parseEnum(new Node(attr), type, readableType);
    }

    public static ChatColor parseChatColor(@Nullable Node node) throws InvalidXMLException {
        return parseEnum(node, ChatColor.class, "color");
    }

    public static ChatColor parseChatColor(@Nullable Node node, ChatColor def) throws InvalidXMLException {
        return node == null ? def : parseChatColor(node);
    }

    public static String getNormalizedNullableText(Element el) {
        String text = el.getTextNormalize();
        if(text == null || "".equals(text)) {
            return null;
        } else {
            return text;
        }
    }

    public static String getNullableAttribute(Element el, String...attrs) {
        String text = null;
        for(String attr : attrs) {
            text = el.getAttributeValue(attr);
            if(text != null) break;
        }
        return text;
    }

    public static UUID parseUuid(@Nullable Node node) throws InvalidXMLException {
        if(node == null) return null;
        String raw = node.getValue();
        try {
            return UUID.fromString(raw);
        }
        catch(IllegalArgumentException e) {
            throw new InvalidXMLException("Invalid UUID format (must be 8-4-4-4-12)", node, e);
        }
    }

    private static final Pattern USERNAME_REGEX = Pattern.compile("[a-zA-Z0-9_]{1,16}");
    public static String parseUsername(@Nullable Node node) throws InvalidXMLException {
        if(node == null) return null;
        String name = node.getValueNormalize();
        if(!USERNAME_REGEX.matcher(name).matches()) {
            throw new InvalidXMLException("Invalid Minecraft username '" + name + "'", node);
        }
        return name;
    }

    public static Skin parseUnsignedSkin(@Nullable Node node) throws InvalidXMLException {
        if(node == null) return null;
        String data = node.getValueNormalize();
        try {
            Base64.decodeBase64(data.getBytes());
        } catch(IllegalArgumentException e) {
            throw new InvalidXMLException("Skin data is not valid base64", node);
        }
        return new Skin(data, null);
    }

    /**
     * Guess if the given text is a JSON object by looking for the curly braces at either end
     */
    public static boolean looksLikeJson(String text) {
        text = text.trim();
        return text.startsWith("{") && text.endsWith("}");
    }

    /**
     * Parse a piece of formatted text, which can be either plain text with legacy
     * formatting codes, or JSON chat components.
     */
    public static BaseComponent parseFormattedText(@Nullable Node node, BaseComponent def) throws InvalidXMLException {
        if(node == null) return def;

        // <blah translate="x"/> is shorthand for <blah>{"translate":"x"}</blah>
        if(node.isElement()) {
            final Attribute translate = node.asElement().getAttribute("translate");
            if(translate != null) {
                return new TranslatableComponent(translate.getValue());
            }
        }

        String text = node.getValueNormalize();
        if(looksLikeJson(text)) {
            try {
                return Components.concat(ComponentSerializer.parse(node.getValue()));
            } catch(JsonParseException e) {
                throw new InvalidXMLException(e.getMessage(), node, e);
            }
        } else {
            return Components.concat(TextComponent.fromLegacyText(BukkitUtils.colorize(text)));
        }
    }

    /**
     * Parse a piece of formatted text, which can be either plain text with legacy
     * formatting codes, or JSON chat components.
     */
    public static @Nullable BaseComponent parseFormattedText(@Nullable Node node) throws InvalidXMLException {
        return parseFormattedText(node, null);
    }

    /**
     * Parse a piece of formatted text, which can be either plain text with legacy
     * formatting codes, or JSON chat components.
     */
    public static BaseComponent parseFormattedText(Element parent, String property, BaseComponent def) throws InvalidXMLException {
        return parseFormattedText(Node.fromChildOrAttr(parent, property), def);
    }

    /**
     * Parse a piece of formatted text, which can be either plain text with legacy
     * formatting codes, or JSON chat components.
     */
    public static BaseComponent parseFormattedText(Element parent, String property) throws InvalidXMLException {
        return parseFormattedText(Node.fromChildOrAttr(parent, property));
    }

    public static BaseComponent parseLocalizedText(Element el) throws InvalidXMLException {
        final Attribute translate = el.getAttribute("translate");
        if(translate != null) {
            return new TranslatableComponent(translate.getValue());
        } else {
            return new Component(el.getTextNormalize());
        }
    }

    public static BaseComponent parseLocalizedText(@Nullable Element el, BaseComponent def) throws InvalidXMLException {
        return el == null ? def : parseLocalizedText(el);
    }

    public static BaseComponent parseLocalizedText(Node node) throws InvalidXMLException {
        if(node.isElement()) {
            return parseLocalizedText(node.asElement());
        } else {
            return new Component(node.getValueNormalize());
        }
    }

    public static PropertyBuilder<String, ?> parseMessageKey(Element el, String name) throws InvalidXMLException {
        return new PropertyBuilder<>(el, name, StringParser.get())
            .validate((value, node) -> {
                if(!Translations.get().hasKey(value)) {
                    throw new InvalidXMLException("Unknown message key '" + value + "'", node);
                }
            });
    }

    public static PropertyBuilder<Team.OptionStatus, ?> parseNameTagVisibility(Element parent, String name) throws InvalidXMLException {
        return new PropertyBuilder<>(parent, name, new TeamRelationParser());
    }

    public static Enchantment parseEnchantment(Node node) throws InvalidXMLException {
        return parseEnchantment(node, node.getValueNormalize());
    }

    public static Enchantment parseEnchantment(Node node, String text) throws InvalidXMLException {
        Enchantment enchantment = Enchantment.getByName(text.toUpperCase().replace(" ", "_"));
        if(enchantment == null) enchantment = NMSHacks.getEnchantment(text);

        if(enchantment == null) {
            throw new InvalidXMLException("Unknown enchantment '" + text + "'", node);
        }

        return enchantment;
    }

    public static org.bukkit.attribute.Attribute parseAttribute(Node node, String text) throws InvalidXMLException {
        return attributeParser.parse(node, text);
    }

    public static org.bukkit.attribute.Attribute parseAttribute(Node node) throws InvalidXMLException {
        return parseAttribute(node, node.getValueNormalize());
    }

    public static AttributeModifier.Operation parseAttributeOperation(Node node, String text) throws InvalidXMLException {
        switch(text.toLowerCase()) {
            case "add": return AttributeModifier.Operation.ADD_NUMBER;
            case "base": return AttributeModifier.Operation.ADD_SCALAR;
            case "multiply": return AttributeModifier.Operation.MULTIPLY_SCALAR_1;
        }
        throw new InvalidXMLException("Unknown attribute modifier operation '" + text + "'", node);
    }

    public static AttributeModifier.Operation parseAttributeOperation(Node node) throws InvalidXMLException {
        return parseAttributeOperation(node, node.getValueNormalize());
    }

    public static AttributeModifier.Operation parseAttributeOperation(Node node, AttributeModifier.Operation def) throws InvalidXMLException {
        return node == null ? def : parseAttributeOperation(node);
    }

    public static Pair<org.bukkit.attribute.Attribute, AttributeModifier> parseCompactAttributeModifier(Node node, String text) throws InvalidXMLException {
        final String[] parts = text.split(":");
        if(parts.length != 3) {
            throw new InvalidXMLException("Bad attribute modifier format", node);
        }

        return Pair.create(
            parseAttribute(node, parts[0]),
            new AttributeModifier(
                "FromXML",
                parseNumber(node, parts[2], Double.class),
                parseAttributeOperation(node, parts[1])
            )
        );
    }

    public static Pair<org.bukkit.attribute.Attribute, AttributeModifier> parseAttributeModifier(Element el) throws InvalidXMLException {
        return Pair.create(
            parseAttribute(new Node(el)),
            new AttributeModifier(
                "FromXML",
                parseNumber(Node.fromRequiredAttr(el, "amount"), Double.class),
                parseAttributeOperation(Node.fromAttr(el, "operation"), AttributeModifier.Operation.ADD_NUMBER)
            )
        );
    }

    public static Pair<org.bukkit.attribute.Attribute, ItemAttributeModifier> parseItemAttributeModifier(Element el) throws InvalidXMLException {
        return Pair.create(
            parseAttribute(new Node(el)),
            new ItemAttributeModifier(
                parseEquipmentSlot(Node.fromAttr(el, "slot"), null),
                parseAttributeModifier(el).second
            )
        );
    }

    public static GameMode parseGameMode(Node node, String text) throws InvalidXMLException {
        text = text.trim();
        try {
            return GameMode.valueOf(text.toUpperCase());
        } catch(IllegalArgumentException e) {
            throw new InvalidXMLException("Unknown game-mode '" + text + "'", node);
        }
    }

    public static GameMode parseGameMode(Node node) throws InvalidXMLException {
        return parseGameMode(node, node.getValueNormalize());
    }

    public static GameMode parseGameMode(Node node, GameMode def) throws InvalidXMLException {
        return node == null ? def : parseGameMode(node);
    }

    public static Path parseRelativePath(Node node) throws InvalidXMLException {
        return parseRelativePath(node, null);
    }

    public static Path parseRelativePath(Node node, Path def) throws InvalidXMLException {
        if(node == null) return def;
        final String text = node.getValueNormalize();
        try {
            Path path = Paths.get(text);
            if(path.isAbsolute()) {
                throw new InvalidPathException(text, "Path is not relative");
            }
            for(Path part : path) {
                if(part.toString().trim().startsWith("..")) {
                    throw new InvalidPathException(text, "Path contains an invalid component");
                }
            }
            return path;
        } catch(InvalidPathException e) {
            throw new InvalidXMLException("Invalid relative path '" + text + "'", node, e);
        }
    }

    public static Path parseRelativePath(Path basePath, Node node, Path def) throws InvalidXMLException {
        if(node == null) return def;

        final Path path;
        try {
            path = basePath.resolve(node.getValue()).toRealPath();
        } catch(IOException e) {
            throw new InvalidXMLException("Error resolving relative file path", node);
        }

        if(!path.startsWith(basePath)) {
            throw new InvalidXMLException("Invalid relative file path", node);
        }

        return path;
    }

    public static Path parseRelativeFolder(Path basePath, Node node, Path def) throws InvalidXMLException {
        Path path = parseRelativePath(basePath, node, def);
        if(!Objects.equals(path, def) && !Files.isDirectory(path)) {
            throw new InvalidXMLException("Folder does not exist", node);
        }
        return path;
    }

    public static SemanticVersion parseSemanticVersion(Node node) throws InvalidXMLException {
        if(node == null) return null;

        String[] parts = node.getValueNormalize().split("\\.", 3);
        if(parts.length < 1 || parts.length > 3) {
            throw new InvalidXMLException("Version must be 1 to 3 whole numbers, separated by periods", node);
        }

        int major = parseNumber(node, parts[0], Integer.class);
        int minor = parts.length < 2 ? 0 : parseNumber(node, parts[1], Integer.class);
        int patch = parts.length < 3 ? 0 : parseNumber(node, parts[2], Integer.class);

        return new SemanticVersion(major, minor, patch);
    }

    public static Slot.Player parsePlayerSlot(Node node) throws InvalidXMLException {
        String value = node.getValue();
        Slot slot;
        try {
            slot = Slot.Player.forIndex(Integer.parseInt(value));
            if(slot == null) {
                throw new InvalidXMLException("Invalid inventory slot index (must be between 0 and 35)", node);
            }
        } catch(NumberFormatException e) {
            slot = Slot.forKey(value);
            if(slot == null) {
                throw new InvalidXMLException("Invalid inventory slot name", node);
            }
        }

        if(slot instanceof Slot.Player) {
            return (Slot.Player) slot;
        }

        throw new InvalidXMLException(value + " is not a player slot", node);
    }

    public static EquipmentSlot parseEquipmentSlot(Node node, EquipmentSlot def) throws InvalidXMLException {
        return node == null ? def : parseEquipmentSlot(node);
    }

    public static EquipmentSlot parseEquipmentSlot(Node node) throws InvalidXMLException {
        final EquipmentSlot slot = parsePlayerSlot(node).toEquipmentSlot();
        if(slot == null) {
            throw new InvalidXMLException("Not an equipment slot", node);
        }
        return slot;
    }

    /**
     * Return the path of the given Element in its Document as a list of indexes
     * relative to parent elements.
     *
     * The root element of a document has an empty path.
     */
    public static TIntList indexPath(Element el) {
        return indexPath(el, 0);
    }

    private static TIntList indexPath(Element child, int size) {
        final Element parent = child.getParentElement();
        if(parent == null) {
            return new TIntArrayList(size);
        } else {
            final TIntList path = indexPath(parent, size + 1);
            final int index = ((BoundedElement) child).indexInParent();
            if(index < 0) {
                throw new IllegalStateException("Parent element " + parent + " does not contain its child element " + child);
            }
            path.add(index);
            return path;
        }
    }
}
