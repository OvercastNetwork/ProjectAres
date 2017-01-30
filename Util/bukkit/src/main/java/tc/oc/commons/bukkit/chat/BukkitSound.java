package tc.oc.commons.bukkit.chat;

import java.util.Objects;

import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.chat.Sound;
import tc.oc.commons.core.util.Utils;

public class BukkitSound implements Sound {

    private final org.bukkit.Sound sound;
    private final float volume;
    private final float pitch;

    public BukkitSound(org.bukkit.Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override public float volume() { return volume; }
    @Override public float pitch() { return pitch; }

    @Override public String name() {
        return NMSHacks.getKey(sound);
    }

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(Sound.class, this, obj, that ->
            name().equals(that.name()) &&
            volume == that.volume() &&
            pitch == that.pitch()
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(name(), volume, pitch);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "{sound=" + sound +
               " volume=" + volume +
               " pitch=" + pitch +
               "}";
    }
}
