package tc.oc.pgm.tracker.damage;

import net.md_5.bungee.api.chat.BaseComponent;

public interface PhysicalInfo extends OwnerInfo {
    String getIdentifier();

    BaseComponent getLocalizedName();
}
