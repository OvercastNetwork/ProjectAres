package tc.oc.pgm.utils;

import java.util.Collection;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class EntityUtils {

    public static Stream<Entity> entities(World world) {
        return world.getEntities().stream();
    }

    public static <T extends Entity> Stream<T> entities(World world, Class<T> entityClass) {
        return world.getEntitiesByClass(entityClass).stream();
    }

    public static boolean isLiving(Entity entity) {
        return entity instanceof LivingEntity && !(entity instanceof ArmorStand);
    }

    public static boolean isLiving(Class<? extends Entity> entity) {
        return LivingEntity.class.isAssignableFrom(entity) && !ArmorStand.class.isAssignableFrom(entity);
    }

    public static boolean isLiving(ProjectileSource source) {
        return source instanceof Entity && isLiving((Entity) source);
    }

    public static <T extends Entity> Collection<? extends T> getNearbyEntities(Location location, Vector range, final Class<T> type) {
        Collection<Entity> filtered =  Collections2.filter(location.getWorld().getNearbyEntities(location, range.getX(), range.getY(), range.getZ()), new Predicate<Entity>() {
            @Override
            public boolean apply(Entity entity) {
                return type.isInstance(entity);
            }
        });
        return (Collection<? extends T>) filtered;
    }

    public static @Nullable <T extends Entity> T getClosestEntity(Location location, Vector range, final Class<T> type) {
        T closest = null;
        double minDistanceSquared = Double.POSITIVE_INFINITY;
        for(Entity entity : location.getWorld().getNearbyEntities(location, range.getX(), range.getY(), range.getZ())) {
            if(type.isInstance(entity)) {
                double distanceSquared = location.distanceSquared(entity.getLocation());
                if(distanceSquared < minDistanceSquared) {
                    minDistanceSquared = distanceSquared;
                    closest = type.cast(entity);
                }
            }
        }
        return closest;
    }
}
