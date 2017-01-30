package tc.oc.commons.bukkit.hologram.content;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a single frame of a {@link tc.oc.commons.bukkit.hologram.Hologram}.
 */
public class HologramFrame implements HologramContent {
    private final List<String> content;

    /**
     * Creates a new {@link HologramFrame} with the specified content.
     *
     * @param content The content to be displayed
     */
    public HologramFrame(String... content) {
        ArrayUtils.reverse(content);
        this.content = Arrays.asList(content);
    }

    /**
     * Gets the multi-line text content contained in the frame.
     *
     * @return The content
     */
    public List<String> getContent() {
        return this.content;
    }

    /**
     * Gets the height of the frame.
     *
     * @return The height of the frame.
     */
    public int getHeight() {
        return this.content.size();
    }

    /**
     * Gets the width of the longest row in the frame.
     *
     * @return The width of the longest row in the frame.
     */
    public int getMaxWidth() {
        int largest = 0;
        for (String row : this.content) {
            largest = Math.max(largest, row.length());
        }

        return largest;
    }

    /**
     * Gets the width of the shortest row in the frame.
     *
     * @return The width of the shortest row in the frame.
     */
    public int getMinWidth() {
        int smallest = Integer.MAX_VALUE;
        for (String row : this.content) {
            smallest = Math.min(smallest, row.length());
        }

        return smallest;
    }

    @Override
    public boolean equals(Object obj) {
        if (!HologramFrame.class.isInstance(obj)) return false;

        HologramFrame hologramFrame = (HologramFrame) obj;
        List<String> content = hologramFrame.getContent();
        return this == hologramFrame || (this.content.size() == content.size() && this.content.containsAll(content));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("content", content.toArray())
                .toString();
    }
}
