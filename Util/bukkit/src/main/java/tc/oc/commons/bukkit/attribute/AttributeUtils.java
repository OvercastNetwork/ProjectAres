package tc.oc.commons.bukkit.attribute;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

public final class AttributeUtils {
    private AttributeUtils() {}

    public static void removeModifier(Attributable attributable, AttributeModifier modifier) {
        for(Attribute attribute : Attribute.values()) {
            final AttributeInstance instance = attributable.getAttribute(attribute);
            if(instance != null) instance.removeModifier(modifier);
        }
    }

    public static void removeAllModifiers(AttributeInstance attributeInstance) {
        attributeInstance.getModifiers().forEach(attributeInstance::removeModifier);
    }

    public static void removeAllModifiers(Attributable attributable, Attribute attribute) {
        final AttributeInstance instance = attributable.getAttribute(attribute);
        if(instance != null) removeAllModifiers(instance);
    }

    public static void removeAllModifiers(Attributable attributable) {
        for(Attribute attribute : Attribute.values()) removeAllModifiers(attributable, attribute);
    }
}
