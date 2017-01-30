package tc.oc.commons.bukkit.hologram.content;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Represents an immutable multi-{@link tc.oc.commons.bukkit.hologram.content.HologramFrame} animation that may be
 * displayed by a {@link tc.oc.commons.bukkit.hologram.Hologram}.
 */
public class HologramAnimation implements HologramContent {
    public static boolean DEFAULT_LOOP = true;
    public static long DEFAULT_FRAME_DELAY = 1;
    public static long DEFAULT_START_DELAY = 0;
    public static long DEFAULT_END_DELAY = 0;

    private final ImmutableList<HologramFrame> frames;
    private final int maxHeight;
    private final boolean loop;
    private final long frameDelay;
    private final long startDelay;
    private final long endDelay;

    /**
     * Creates a new animation with the specified parameters.
     *
     * @param loop      Whether or not to loop the animation at its conclusion.
     * @param frameDelay The delay, in ticks.
     * @param frames    The frames to be displayed.
     */
    public HologramAnimation(boolean loop, long frameDelay, long startDelay, long endDelay, HologramFrame... frames) {
        Preconditions.checkArgument(frameDelay >= 1, "Tick delay must be at least 1 (was {0})", frameDelay);
        Preconditions.checkNotNull(frames, "Frames");
        Preconditions.checkArgument(frames.length > 1, "Frame count must be greater than 1 (was {0})", frames.length);
        this.frames = ImmutableList.copyOf(frames);

        int largest = 0;
        for (HologramFrame frame : this.frames) {
            largest = Math.max(largest, frame.getHeight());
        }
        this.maxHeight = largest;

        this.loop = loop;
        this.frameDelay = frameDelay;
        this.startDelay = startDelay;
        this.endDelay = endDelay;
    }

    /**
     * Creates a new animation with the specified parameters and the default tick delay.
     *
     * @param loop   Whether or not to loop the animation at its conclusion.
     * @param frames The frames to be displayed.
     * @see #DEFAULT_FRAME_DELAY
     */
    public HologramAnimation(boolean loop, HologramFrame... frames) {
        this(loop, DEFAULT_FRAME_DELAY, DEFAULT_START_DELAY, DEFAULT_END_DELAY, frames);
    }

    /**
     * Creates a new animation with the specified frames, and the defaults for loop status and tick delay.
     *
     * @param frames The frames to be displayed.
     * @see #DEFAULT_LOOP
     * @see #DEFAULT_FRAME_DELAY
     */
    public HologramAnimation(HologramFrame... frames) {
        this(DEFAULT_LOOP, DEFAULT_FRAME_DELAY, DEFAULT_START_DELAY, DEFAULT_END_DELAY, frames);
    }

    /**
     * Gets the frames of the animation.
     *
     * @return The frames of the animation.
     */
    public ImmutableList<HologramFrame> getFrames() {
        return this.frames;
    }

    /**
     * Gets whether or not the animation should loop.
     *
     * @return Whether or not the animation should loop.
     */
    public boolean shouldLoop() {
        return this.loop;
    }

    /**
     * Gets the number of ticks that the hologram should wait in between frames.
     *
     * @return The number of ticks that the hologram should wait in between frames.
     */
    public long getFrameDelay() {
        return this.frameDelay;
    }

    public long getStartDelay() {
        return this.startDelay;
    }

    public long getEndDelay() {
        return this.endDelay;
    }

    /**
     * Gets the vertical height of the tallest frame in the animation.
     *
     * @return The vertical height of the tallest frame in the animation.
     */
    public int getMaxHeight() {
        return this.maxHeight;
    }
}
