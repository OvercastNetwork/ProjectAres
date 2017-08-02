package tc.oc.commons.bukkit.item;

import net.minecraft.server.NBTTagCompound;

public class IntegerItemTag extends ItemTag<Integer> {

    public IntegerItemTag(String name, Integer defaultValue) {
        super(name, defaultValue);
    }

    @Override
    protected boolean hasPrimitive(NBTTagCompound tag) {
        return tag.hasKeyOfType(name, 3);
    }

    @Override
    protected Integer getPrimitive(NBTTagCompound tag) {
        return tag.getInt(name);
    }

    @Override
    protected void setPrimitive(NBTTagCompound tag, Integer value) {
        tag.setInt(name, value);
    }
}
