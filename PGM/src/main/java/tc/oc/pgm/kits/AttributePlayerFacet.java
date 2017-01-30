package tc.oc.pgm.kits;

import javax.inject.Inject;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.commons.core.util.MultimapHelper;
import tc.oc.pgm.events.PlayerResetEvent;
import tc.oc.pgm.match.MatchPlayerFacet;

public class AttributePlayerFacet implements MatchPlayerFacet, Listener {

    private final Player player;
    private final SetMultimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();

    @Inject AttributePlayerFacet(Player player) {
        this.player = player;
    }

    private boolean addModifier0(Attribute attribute, AttributeModifier modifier) {
        final AttributeInstance attributeInstance = player.getAttribute(attribute);
        if(attributeInstance != null && !attributeInstance.getModifiers().contains(modifier)) {
            attributeInstance.addModifier(modifier);
            return true;
        }
        return false;
    }

    public boolean addModifier(Attribute attribute, AttributeModifier modifier) {
        return modifiers.put(attribute, modifier) && addModifier0(attribute, modifier);
    }

    private boolean removeModifier0(Attribute attribute, AttributeModifier modifier) {
        AttributeInstance attributeValue = player.getAttribute(attribute);
        if(attributeValue != null && attributeValue.getModifiers().contains(modifier)) {
            attributeValue.removeModifier(modifier);
            return true;
        }
        return false;
    }

    public boolean removeModifier(Attribute attribute, AttributeModifier modifier) {
        return modifiers.remove(attribute, modifier) && removeModifier0(attribute, modifier);
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerReset(final PlayerResetEvent event) {
        MultimapHelper.forEach(modifiers, this::removeModifier0);
        modifiers.clear();
    }
}
