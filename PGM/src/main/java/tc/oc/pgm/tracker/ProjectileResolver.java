package tc.oc.pgm.tracker;

import javax.annotation.Nullable;

import org.bukkit.projectiles.ProjectileSource;
import tc.oc.pgm.tracker.damage.PhysicalInfo;

public interface ProjectileResolver {

    @Nullable
    PhysicalInfo resolveShooter(ProjectileSource source);
}
