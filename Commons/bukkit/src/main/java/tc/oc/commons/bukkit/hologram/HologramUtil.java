package tc.oc.commons.bukkit.hologram;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

/**
 * Various {@link tc.oc.commons.bukkit.hologram.content.HologramFrame}-related utility methods
 */
public class HologramUtil {
    public static final int MAX_CLOSEST = 255 * 255 * 3;

    /**
     * The filler content used when transparency is detected inside of an image.
     *
     * @see <a href="http://www.fileformat.info/info/unicode/char/23b9/index.htm">FileFormat.info reference</a>.
     */
    public static final String ALPHA_FILLER_CONTENT = ChatColor.DARK_GRAY + "  \u23B9";

    /**
     * The character used for non-transparent pixels inside of an image.
     *
     * @see <a href="http://www.unicodemap.org/details/0x2588/index.html">UnicodeMap.org reference</a>.
     */
    public static final char PIXEL_CHAR = '\u2588';

    /**
     * A map of Minecraft chat colors to their closest RGB counterparts, created by asdjke.
     */
    public static final ImmutableBiMap<Color, ChatColor> COLOR_MAP = ImmutableBiMap.<Color, ChatColor>builder()
            .put(Color.BLACK, ChatColor.BLACK)
            .put(new Color(0, 0, 170), ChatColor.DARK_BLUE)
            .put(new Color(0, 170, 0), ChatColor.DARK_GREEN)
            .put(new Color(0, 170, 170), ChatColor.DARK_AQUA)
            .put(new Color(170, 0, 0), ChatColor.DARK_RED)
            .put(new Color(170, 0, 170), ChatColor.DARK_PURPLE)
            .put(new Color(255, 170, 0), ChatColor.GOLD)
            .put(new Color(170, 170, 170), ChatColor.GRAY)
            .put(new Color(85, 85, 85), ChatColor.DARK_GRAY)
            .put(new Color(85, 85, 255), ChatColor.BLUE)
            .put(new Color(85, 255, 85), ChatColor.GREEN)
            .put(new Color(85, 255, 255), ChatColor.AQUA)
            .put(new Color(255, 85, 85), ChatColor.RED)
            .put(new Color(255, 85, 255), ChatColor.LIGHT_PURPLE)
            .put(new Color(255, 255, 85), ChatColor.YELLOW)
            .put(Color.WHITE, ChatColor.WHITE)
            .build();

    /**
     * Converts a {@link java.awt.image.BufferedImage} to a multi-line text message, using {@link #COLOR_MAP}.
     *
     * @return A {@link java.lang.String[]} containing the message
     */
    public static String[] imageToText(BufferedImage image, boolean trim) {
        int height = Preconditions.checkNotNull(image, "Image").getHeight();
        int width = image.getWidth();

        String[][] message = new String[height][width];
        LinkedList<Integer> pendingAlpha = new LinkedList<>();
        for (int y = 0; y < height; y++) {
            boolean fillAlpha = !trim;
            boolean left = false;

            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y), true);

                if (trim) {
                    if (color.getAlpha() < 1) {
                        pendingAlpha.add(x);
                        left = (left || x == 0);
                    } else {
                        if (!left) {
                            applyPendingAlpha(pendingAlpha, message[y]);
                        } else {
                            pendingAlpha.clear();
                            left = false;
                        }
                    }
                }

                ChatColor minecraftColor = rgbToMinecraft(closestColorMatch(color, COLOR_MAP.keySet()));
                message[y][x] = minecraftColor == null ? (fillAlpha ? ALPHA_FILLER_CONTENT : "") : minecraftColor.toString() + PIXEL_CHAR;
            }

            if (!trim) {
                applyPendingAlpha(pendingAlpha, message[y]);
            }
        }

        String[] messageFinal = new String[height];
        for (int y = 0; y < height; y++) {
            messageFinal[y] = StringUtils.join(message[y]);
        }

        return messageFinal;
    }

    /**
     * Attempts to find the closest {@link ChatColor} for the given {@link Color}.
     *
     * @param color The color
     * @return The appropriate chat color
     */
    public static @Nullable ChatColor rgbToMinecraft(Color color) {
        return COLOR_MAP.get(color);
    }

    /**
     * Finds the closest match for the specified {@link Color} from the provided colors. Created by asdjke.
     *
     * @param color  The color to match
     * @param colors The possible matches
     * @return The closest match, or null if alpha is zero
     */
    public static @Nullable Color closestColorMatch(Color color, Iterable<Color> colors) {
        if (color.getAlpha() < 1) return null;

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        int closest = MAX_CLOSEST;
        Color best = null;

        for (Color key : colors) {
            int rDist = Math.abs(r - key.getRed());
            int gDist = Math.abs(g - key.getGreen());
            int bDist = Math.abs(b - key.getBlue());
            int dist = rDist * rDist + gDist * gDist + bDist * bDist;
            if (dist < closest) {
                best = key;
                closest = dist;
            }
        }

        return best;
    }

    private static void applyPendingAlpha(LinkedList<Integer> pendingAlpha, String[] message) {
        for (int x : pendingAlpha) {
            message[x] = ALPHA_FILLER_CONTENT;
        }
        pendingAlpha.clear();
    }
}
