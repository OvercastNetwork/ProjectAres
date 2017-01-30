package tc.oc.pgm.classes;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import tc.oc.commons.core.util.Utils;
import tc.oc.pgm.kits.Kit;

public class PlayerClass {

    private final String name;
    private final String category;
    private final @Nullable String description;
    private final @Nullable String longDescription;
    private final boolean sticky;
    private final Set<Kit> kits;
    private final MaterialData icon;
    private final boolean restrict;

    public PlayerClass(String name, String category, @Nullable String description, @Nullable String longDescription, boolean sticky, Set<Kit> kits, MaterialData icon, boolean restrict) {
        this.name = checkNotNull(name, "name");
        this.category = checkNotNull(category, "family name");
        this.description = description;
        this.longDescription = longDescription;
        this.sticky = sticky;
        this.kits = ImmutableSet.copyOf(checkNotNull(kits, "kits"));
        this.icon = checkNotNull(icon, "icon");
        this.restrict = restrict;
    }

    public String getName() {
        return this.name;
    }

    public String getCategory() {
        return this.category;
    }

    public @Nullable String getDescription() {
        return this.description;
    }

    public @Nullable String getLongDescription() {
        return this.longDescription;
    }

    public boolean isSticky() {
        return this.sticky;
    }

    public Set<Kit> getKits() {
        return this.kits;
    }

    public MaterialData getIcon() {
        return this.icon;
    }

    public boolean isRestricted() {
        return this.restrict;
    }

    public boolean canUse(Player player) {
        return !this.isRestricted() || player.isOp();
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, name);
    }

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(PlayerClass.class, this, obj, that ->
            this.getCategory().equals(that.getCategory()) &&
            this.getName().equals(that.getName())
        );
    }
}
