package tc.oc.commons.bukkit.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffectType;
import tc.oc.commons.core.chat.ChatUtils;
import tc.oc.commons.core.chat.Component;

public class BukkitUtils {
    private static final ItemFactory factory = Bukkit.getServer().getItemFactory();

    /** Makes strings have pretty colors */
    public static String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', ChatColor.translateAlternateColorCodes('`', s));
    }

    public static String decolorize(String s) {
        return ChatColor.stripColor(colorize(s));
    }

    public static List<String> colorizeList(List<String> list) {
        List<String> result = new ArrayList<>();

        for (String line : list) {
            result.add(colorize(line));
        }

        return result;
    }

    public static String escapeColors(String text) {
        return escapeColors(text, '&');
    }

    public static String escapeColors(String text, char placeholder) {
        return text.replace(ChatColor.COLOR_CHAR, placeholder);
    }

    public static String woolMessage(DyeColor color) {
        return dyeColorToChatColor(color) + color.toString().replace("_", " ") + " WOOL";
    }

    public static BaseComponent woolName(DyeColor color) {
        return new Component(color.toString().replace("_", " ") + " WOOL", ChatUtils.convert(dyeColorToChatColor(color)));
    }

    public static ItemStack generateBook(String title, String author, List<String> pages) {
        ItemStack stack = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) factory.getItemMeta(Material.WRITTEN_BOOK);

        meta.setTitle(title);
        meta.setAuthor(author);
        meta.setPages(pages);

        stack.setItemMeta(meta);
        return stack;
    }

    public static ChatColor dyeColorToChatColor(DyeColor dyeColor) {
        ChatColor chatColor = DYE_CHAT_MAP.get(dyeColor);
        if (chatColor != null) {
            return chatColor;
        } else {
            return ChatColor.WHITE;
        }
    }

    public static net.md_5.bungee.api.ChatColor toChatColor(DyeColor dyeColor) {
        return convertColor(dyeColorToChatColor(dyeColor));
    }

    public static DyeColor chatColorToDyeColor(ChatColor chatColor) {
        DyeColor dyeColor = CHAT_DYE_MAP.get(chatColor);
        if (dyeColor != null) {
            return dyeColor;
        } else {
            return DyeColor.WHITE;
        }
    }

    public static DyeColor chatColorToDyeColor(net.md_5.bungee.api.ChatColor chatColor) {
        return chatColorToDyeColor(convertColor(chatColor));
    }

    private static final Map<DyeColor, ChatColor> DYE_CHAT_MAP = ImmutableMap.<DyeColor, ChatColor>builder()
            .put(DyeColor.BLACK, ChatColor.BLACK)
            .put(DyeColor.BLUE, ChatColor.BLUE) // DARK_BLUE always looks ugly in chat
            .put(DyeColor.GREEN, ChatColor.DARK_GREEN)
            .put(DyeColor.CYAN, ChatColor.DARK_AQUA)
            .put(DyeColor.RED, ChatColor.DARK_RED)
            .put(DyeColor.PURPLE, ChatColor.DARK_PURPLE)
            .put(DyeColor.ORANGE, ChatColor.GOLD)
            .put(DyeColor.SILVER, ChatColor.GRAY)
            .put(DyeColor.GRAY, ChatColor.DARK_GRAY)
            .put(DyeColor.LIGHT_BLUE, ChatColor.BLUE)
            .put(DyeColor.LIME, ChatColor.GREEN)
            .put(DyeColor.BROWN, ChatColor.DARK_RED)
            .put(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE)
            .put(DyeColor.YELLOW, ChatColor.YELLOW)
            .put(DyeColor.WHITE, ChatColor.WHITE)
            .put(DyeColor.PINK, ChatColor.RED)
            .build();

    private static final Map<ChatColor, DyeColor> CHAT_DYE_MAP = ImmutableMap.<ChatColor, DyeColor>builder()
            .put(ChatColor.AQUA, DyeColor.LIGHT_BLUE)
            .put(ChatColor.BLACK, DyeColor.BLACK)
            .put(ChatColor.BLUE, DyeColor.BLUE)
            .put(ChatColor.DARK_AQUA, DyeColor.CYAN)
            .put(ChatColor.DARK_BLUE, DyeColor.BLUE)
            .put(ChatColor.DARK_GRAY, DyeColor.GRAY)
            .put(ChatColor.DARK_GREEN, DyeColor.GREEN)
            .put(ChatColor.DARK_PURPLE, DyeColor.PURPLE)
            .put(ChatColor.DARK_RED, DyeColor.RED)
            .put(ChatColor.GOLD, DyeColor.ORANGE)
            .put(ChatColor.GRAY, DyeColor.SILVER)
            .put(ChatColor.GREEN, DyeColor.LIME)
            .put(ChatColor.LIGHT_PURPLE, DyeColor.MAGENTA)
            .put(ChatColor.RED, DyeColor.PINK)
            .put(ChatColor.WHITE, DyeColor.WHITE)
            .put(ChatColor.YELLOW, DyeColor.YELLOW).build();

    public static Color colorOf(ChatColor chatColor) {
        Color color = CHAT_COLOR_MAP.get(chatColor);
        if (color != null) {
            return color;
        } else {
            return Color.WHITE;
        }
    }

    public static Color colorOf(net.md_5.bungee.api.ChatColor chatColor) {
        return colorOf(convertColor(chatColor));
    }

    private static final Map<ChatColor, Color> CHAT_COLOR_MAP = ImmutableMap.<ChatColor, Color>builder()
            .put(ChatColor.BLACK, Color.fromRGB(0, 0, 0))
            .put(ChatColor.DARK_BLUE, Color.fromRGB(0, 0, 170))
            .put(ChatColor.DARK_GREEN, Color.fromRGB(0, 170, 0))
            .put(ChatColor.DARK_AQUA, Color.fromRGB(0, 170, 170))
            .put(ChatColor.DARK_RED, Color.fromRGB(170, 0, 0))
            .put(ChatColor.DARK_PURPLE, Color.fromRGB(170, 0, 170))
            .put(ChatColor.GOLD, Color.fromRGB(255, 170, 0))
            .put(ChatColor.GRAY, Color.fromRGB(170, 170, 170))
            .put(ChatColor.DARK_GRAY, Color.fromRGB(85, 85, 85))
            .put(ChatColor.BLUE, Color.fromRGB(85, 85, 255))
            .put(ChatColor.GREEN, Color.fromRGB(85, 255, 85))
            .put(ChatColor.AQUA, Color.fromRGB(85, 255, 255))
            .put(ChatColor.RED, Color.fromRGB(255, 85, 85))
            .put(ChatColor.LIGHT_PURPLE, Color.fromRGB(255, 85, 255))
            .put(ChatColor.YELLOW, Color.fromRGB(255, 255, 85))
            .put(ChatColor.WHITE, Color.fromRGB(255, 255, 255)).build();

    public static String potionEffectTypeName(final PotionEffectType type) {
        String name = POTION_EFFECT_MAP.get(type);
        if (name != null) {
            return name;
        } else {
            return "Unknown";
        }
    }

    public static final Map<PotionEffectType, String> POTION_EFFECT_MAP = ImmutableMap.<PotionEffectType, String>builder()
            .put(PotionEffectType.BLINDNESS, "Blindness")
            .put(PotionEffectType.CONFUSION, "Nausea")
            .put(PotionEffectType.DAMAGE_RESISTANCE, "Resistance")
            .put(PotionEffectType.FAST_DIGGING, "Haste")
            .put(PotionEffectType.FIRE_RESISTANCE, "Fire Resistance")
            .put(PotionEffectType.HARM, "Instant Damage")
            .put(PotionEffectType.HEAL, "Instant Health")
            .put(PotionEffectType.HUNGER, "Hunger")
            .put(PotionEffectType.INCREASE_DAMAGE, "Strength")
            .put(PotionEffectType.INVISIBILITY, "Invisibility")
            .put(PotionEffectType.JUMP, "Jump Boost")
            .put(PotionEffectType.NIGHT_VISION, "Night Vision")
            .put(PotionEffectType.POISON, "Poison")
            .put(PotionEffectType.REGENERATION, "Regeneration")
            .put(PotionEffectType.SLOW, "Slowness")
            .put(PotionEffectType.SLOW_DIGGING, "Mining Fatigue")
            .put(PotionEffectType.SPEED, "Speed")
            .put(PotionEffectType.WATER_BREATHING, "Water Breating")
            .put(PotionEffectType.WEAKNESS, "Weakness")
            .put(PotionEffectType.WITHER, "Wither")
            .put(PotionEffectType.HEALTH_BOOST, "Health Boost")
            .put(PotionEffectType.ABSORPTION, "Absorption")
            .put(PotionEffectType.SATURATION, "Saturation")
            .build();

    public static ChatColor convertColor(net.md_5.bungee.api.ChatColor color) {
        return ChatColor.getByChar(color.toString().charAt(1));
    }

    public static net.md_5.bungee.api.ChatColor convertColor(ChatColor color) {
        return net.md_5.bungee.api.ChatColor.getByChar(color.toString().charAt(1));
    }

}
