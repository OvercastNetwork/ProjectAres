package tc.oc.pgm.kits.tag;

import tc.oc.commons.bukkit.item.BooleanItemTag;
import tc.oc.commons.bukkit.item.StringItemTag;

public class ItemTags {

    public static final BooleanItemTag PREVENT_SHARING = new BooleanItemTag("prevent-sharing", false);
    public static final BooleanItemTag LOCKED = new BooleanItemTag("locked", false);
    public static final StringItemTag KIT = new StringItemTag("victim-kit", null);
    public static final StringItemTag HITTER_KIT = new StringItemTag("attacker-kit", null);

    private ItemTags() {}
}
