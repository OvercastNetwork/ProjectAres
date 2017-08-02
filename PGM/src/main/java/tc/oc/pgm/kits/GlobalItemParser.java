package tc.oc.pgm.kits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.ItemAttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jdom2.Element;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.util.Pair;
import tc.oc.pgm.kits.tag.Grenade;
import tc.oc.pgm.kits.tag.ItemTags;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.parser.ElementParser;
import tc.oc.pgm.xml.parser.Parser;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowFunction;

/**
 * Item parser with no MapScoped dependencies, so it can be used outside of any specific map.
 *
 * May be missing some features that require a map.
 */
public class GlobalItemParser implements ElementParser<ItemStack> {

    private final Parser<Material> materialParser;

    @Inject protected GlobalItemParser(Parser<Material> materialParser) {
        this.materialParser = materialParser;
    }

    @Override
    public @Nullable ItemStack parseElement(@Nullable Element element) throws InvalidXMLException {
        return parseItem(element, true);
    }

    public ItemStack parseRequiredItem(Element parent) throws InvalidXMLException {
        final Element el = XMLUtils.getRequiredUniqueChild(parent);
        switch(el.getName()) {
            case "item": return parseItem(el, false);
            case "head": return parseItem(el, Material.SKULL_ITEM, (short) 3);
            case "book": return parseItem(el, Material.WRITTEN_BOOK);
        }
        throw new InvalidXMLException("Item expected", el);
    }

    public ItemStack parseBook(Element el) throws InvalidXMLException {
        return parseItem(el, Material.WRITTEN_BOOK);
    }

    public ItemStack parseHead(Element el) throws InvalidXMLException {
        return parseItem(el, Material.SKULL_ITEM, (short) 3);
    }

    public @Nullable ItemStack parseItem(@Nullable Element el, boolean allowAir) throws InvalidXMLException {
        if (el == null) return null;

        final Node materialNode = Optional.ofNullable(el.getAttribute("material"))
                                          .map(Node::of)
                                          .orElseGet(() -> Node.of(el));
        final Material material = materialParser.parse(materialNode);

        if(material == Material.AIR && !allowAir) {
            throw new InvalidXMLException("Material AIR is not allowed here", materialNode);
        }

        return parseItem(el, material);
    }

    public ItemStack parseItem(Element el, Material type) throws InvalidXMLException {
        return parseItem(el, type, XMLUtils.parseNumber(el.getAttribute("damage"), Short.class, (short) 0));
    }

    public ItemStack parseItem(Element el, Material type, short damage) throws InvalidXMLException {
        int amount = XMLUtils.parseNumber(el.getAttribute("amount"), Integer.class, 1);

        // If the item is a potion with non-zero damage, and there is
        // no modern potion ID, decode the legacy damage value.
        final Potion legacyPotion;
        if(type == Material.POTION && damage > 0 && el.getAttribute("potion") == null) {
            try {
                legacyPotion = Potion.fromDamage(damage);
            } catch(IllegalArgumentException e) {
                throw new InvalidXMLException("Invalid legacy potion damage value " + damage + ": " + e.getMessage(), el, e);
            }

            // If the legacy splash bit is set, convert to a splash potion
            if(legacyPotion.isSplash()) {
                type = Material.SPLASH_POTION;
                legacyPotion.setSplash(false);
            }

            // Potions always have damage 0
            damage = 0;
        } else {
            legacyPotion = null;
        }

        ItemStack itemStack = new ItemStack(type, amount, damage);
        if(itemStack.getType() != type) {
            throw new InvalidXMLException("Invalid item/block", el);
        }

        final ItemMeta meta = itemStack.getItemMeta();
        if(meta != null) { // This happens if the item is "air"
            parseItemMeta(el, meta);

            // If we decoded a legacy potion, apply it now, but only if there are no custom effects.
            // This emulates the old behavior of custom effects overriding default effects.
            if(legacyPotion != null) {
                final PotionMeta potionMeta = (PotionMeta) meta;
                if(!potionMeta.hasCustomEffects()) {
                    potionMeta.setBasePotionData(new PotionData(legacyPotion.getType(),
                                                                legacyPotion.hasExtendedDuration(),
                                                                legacyPotion.getLevel() == 2));
                }
            }

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    public void parseItemMeta(Element el, ItemMeta meta) throws InvalidXMLException {
        parseEnchantments(el, "enchantment").forEach(
            (enchantment, level) -> meta.addEnchant(enchantment, level, true)
        );

        if(meta instanceof EnchantmentStorageMeta) {
            parseEnchantments(el, "stored-enchantment").forEach(
                (enchantment, level) -> ((EnchantmentStorageMeta) meta).addStoredEnchant(enchantment, level, true)
            );
        }

        if(meta instanceof PotionMeta) {
            final PotionMeta potionMeta = (PotionMeta) meta;

            final Node potionAttr = Node.fromAttr(el, "potion");
            if(potionAttr != null) {
                potionMeta.setPotionBrew(XMLUtils.parsePotion(potionAttr));
            }

            final List<PotionEffect> effects = parsePotionEffects(el);

            for(PotionEffect effect : potionMeta.getCustomEffects()) {
                potionMeta.removeCustomEffect(effect.getType());
            }

            for(PotionEffect effect : effects) {
                potionMeta.addCustomEffect(effect, false);
            }
        }

        for(Map.Entry<Attribute, ItemAttributeModifier> entry : parseItemAttributeModifiers(el).entries()) {
            meta.addAttributeModifier(entry.getKey(), entry.getValue());
        }

        String customName = el.getAttributeValue("name");
        if(customName != null) {
            meta.setDisplayName(BukkitUtils.colorize(customName));
        } else if (XMLUtils.parseBoolean(el.getAttribute("grenade"), false)) {
            meta.setDisplayName("Grenade");
        }

        if(meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta armorMeta = (LeatherArmorMeta) meta;
            org.jdom2.Attribute attrColor = el.getAttribute("color");
            if(attrColor != null) {
                String raw = attrColor.getValue();
                if(!raw.matches("[a-fA-F0-9]{6}")) {
                    throw new InvalidXMLException("Invalid color format", attrColor);
                }
                armorMeta.setColor(Color.fromRGB(Integer.parseInt(attrColor.getValue(), 16)));
            }
        }

        String loreText = el.getAttributeValue("lore");
        if(loreText != null) {
            List<String> lore = ImmutableList.copyOf(Splitter.on('|').split(BukkitUtils.colorize(loreText)));
            meta.setLore(lore);
        }

        for(ItemFlag flag : ItemFlag.values()) {
            if(!XMLUtils.parseBoolean(Node.fromAttr(el, "show-" + itemFlagName(flag)), true)) {
                meta.addItemFlags(flag);
            }
        }

        if(XMLUtils.parseBoolean(el.getAttribute("unbreakable"), false)) {
            meta.setUnbreakable(true);
        }

        Element elCanDestroy = el.getChild("can-destroy");
        if(elCanDestroy != null) {
            meta.setCanDestroy(XMLUtils.parseMaterialMatcher(elCanDestroy).getMaterials());
        }

        Element elCanPlaceOn = el.getChild("can-place-on");
        if(elCanPlaceOn != null) {
            meta.setCanPlaceOn(XMLUtils.parseMaterialMatcher(elCanPlaceOn).getMaterials());
        }

        if(meta instanceof SkullMeta) {
            final Node skin = Node.fromChildOrAttr(el, "skin");
            if(skin != null) {
                ((SkullMeta) meta).setOwner(XMLUtils.parseUsername(Node.fromChildOrAttr(el, "username")),
                                            Node.childOrAttr(el, "uuid")
                                                .map(rethrowFunction(XMLUtils::parseUuid))
                                                .orElseGet(UUID::randomUUID),
                                            XMLUtils.parseUnsignedSkin(Node.fromRequiredChildOrAttr(el, "skin")));
            }
        }

        if(meta instanceof BookMeta) {
            final BookMeta book = (BookMeta) meta;

            Node.childOrAttr(el, "title").ifPresent(
                node -> book.setTitle(BukkitUtils.colorize(node.getValue()))
            );
            Node.childOrAttr(el, "author").ifPresent(
                node -> book.setAuthor(BukkitUtils.colorize(node.getValue()))
            );

            Element elPages = el.getChild("pages");
            if(elPages != null) {
                for(Element elPage : elPages.getChildren("page")) {
                    String text = elPage.getText();
                    text = text.trim(); // Remove leading and trailing whitespace
                    text = Pattern.compile("^[ \\t]+", Pattern.MULTILINE).matcher(text).replaceAll(""); // Remove indentation on each line
                    text = Pattern.compile("^\\n", Pattern.MULTILINE).matcher(text).replaceAll(" \n"); // Add a space to blank lines, otherwise they vanish for unknown reasons
                    text = BukkitUtils.colorize(text); // Color codes
                    book.addPage(text);
                }
            }
        }

        parseCustomNBT(el, meta);
    }

    String itemFlagName(ItemFlag flag) {
        switch(flag) {
            case HIDE_ATTRIBUTES: return "attributes";
            case HIDE_ENCHANTS: return "enchantments";
            case HIDE_UNBREAKABLE: return "unbreakable";
            case HIDE_DESTROYS: return "can-destroy";
            case HIDE_PLACED_ON: return "can-place-on";
            case HIDE_POTION_EFFECTS: return "other";
        }
        throw new IllegalStateException("Unknown item flag " + flag);
    }

    public void parseCustomNBT(Element el, ItemMeta meta) throws InvalidXMLException {
        if (XMLUtils.parseBoolean(el.getAttribute("grenade"), false)) {
            Grenade.ITEM_TAG.set(meta, new Grenade(
                XMLUtils.parseNumber(el.getAttribute("grenade-power"), Float.class, 1f),
                XMLUtils.parseBoolean(el.getAttribute("grenade-fire"), false),
                XMLUtils.parseBoolean(el.getAttribute("grenade-destroy"), true)
            ));
        }

        if(XMLUtils.parseBoolean(el.getAttribute("prevent-sharing"), false)) {
            ItemTags.PREVENT_SHARING.set(meta, true);
        }

        if(XMLUtils.parseBoolean(el.getAttribute("locked"), false)) {
            ItemTags.LOCKED.set(meta, true);
        }
    }

    public Pair<Enchantment, Integer> parseEnchantment(Element el) throws InvalidXMLException {
        return Pair.create(XMLUtils.parseEnchantment(new Node(el)),
                           XMLUtils.parseNumber(Node.fromAttr(el, "level"), Integer.class, 1));
    }

    public Map<Enchantment, Integer> parseEnchantments(Element el, String name) throws InvalidXMLException {
        Map<Enchantment, Integer> enchantments = Maps.newHashMap();

        Node attr = Node.fromAttr(el, name, StringUtils.pluralize(name));
        if(attr != null) {
            Iterable<String> enchantmentTexts = Splitter.on(";").split(attr.getValue());
            for(String enchantmentText : enchantmentTexts) {
                int level = 1;
                List<String> parts = Lists.newArrayList(Splitter.on(":").limit(2).split(enchantmentText));
                Enchantment enchant = XMLUtils.parseEnchantment(attr, parts.get(0));
                if(parts.size() > 1) {
                    level = XMLUtils.parseNumber(attr, parts.get(1), Integer.class);
                }
                enchantments.put(enchant, level);
            }
        }

        for(Element elEnchantment : el.getChildren(name)) {
            Pair<Enchantment, Integer> entry = parseEnchantment(elEnchantment);
            enchantments.put(entry.first, entry.second);
        }

        return enchantments;
    }

    public SetMultimap<Attribute, AttributeModifier> parseAttributeModifiers(Element el) throws InvalidXMLException {
        SetMultimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();

        Node attr = Node.fromAttr(el, "attribute", "attributes");
        if(attr != null) {
            for(String modifierText : Splitter.on(";").split(attr.getValue())) {
                Pair<Attribute, AttributeModifier> mod = XMLUtils.parseCompactAttributeModifier(attr, modifierText);
                modifiers.put(mod.first, mod.second);
            }
        }

        for(Element elAttribute : el.getChildren("attribute")) {
            Pair<Attribute, AttributeModifier> mod = XMLUtils.parseAttributeModifier(elAttribute);
            modifiers.put(mod.first, mod.second);
        }

        return modifiers;
    }

    public SetMultimap<Attribute, ItemAttributeModifier> parseItemAttributeModifiers(Element el) throws InvalidXMLException {
        SetMultimap<Attribute, ItemAttributeModifier> modifiers = HashMultimap.create();

        Node attr = Node.fromAttr(el, "attribute", "attributes");
        if(attr != null) {
            for(String modifierText : Splitter.on(";").split(attr.getValue())) {
                Pair<Attribute, AttributeModifier> mod = XMLUtils.parseCompactAttributeModifier(attr, modifierText);
                modifiers.put(mod.first, new ItemAttributeModifier(null, mod.second));
            }
        }

        for(Element elAttribute : el.getChildren("attribute")) {
            Pair<Attribute, ItemAttributeModifier> mod = XMLUtils.parseItemAttributeModifier(elAttribute);
            modifiers.put(mod.first, mod.second);
        }

        return modifiers;
    }

    public List<PotionEffect> parsePotionEffects(Element el) throws InvalidXMLException {
        List<PotionEffect> effects = new ArrayList<>();

        Node attr = Node.fromAttr(el, "effect", "effects", "potions");
        if(attr != null) {
            for(String piece : attr.getValue().split(";")) {
                effects.add(checkPotionEffect(XMLUtils.parseCompactPotionEffect(attr, piece), attr));
            }
        }

        for(Element elPotion : XMLUtils.getChildren(el, "effect", "potion")) {
            effects.add(parsePotionEffect(elPotion));
        }

        return effects;
    }

    public PotionEffect parsePotionEffect(Element el) throws InvalidXMLException {
        return checkPotionEffect(XMLUtils.parsePotionEffect(el), new Node(el));
    }

    private PotionEffect checkPotionEffect(PotionEffect effect, Node node) throws InvalidXMLException {
        if(effect.getType().equals(PotionEffectType.HEALTH_BOOST) && effect.getAmplifier() < 0) {
            if(effect.getDuration() != Integer.MAX_VALUE) {
                // TODO: enable this check after existing maps are fixed
                // throw new InvalidXMLException("Negative health boost effect must have infinite duration (use max-health instead)", node);
            }
        }
        return effect;
    }

}
