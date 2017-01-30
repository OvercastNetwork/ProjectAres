package tc.oc.commons.bukkit.item;

import net.minecraft.server.NBTTagCompound;

public class FloatItemTag extends ItemTag<Float> {

    public FloatItemTag(String name, Float defaultValue) {
        super(name, defaultValue);
    }

    @Override
    protected boolean hasPrimitive(NBTTagCompound tag) {
        return tag.hasKeyOfType(name, 5);
    }

    @Override
    protected Float getPrimitive(NBTTagCompound tag) {
        return tag.getFloat(name);
    }

    @Override
    protected void setPrimitive(NBTTagCompound tag, Float value) {
        tag.setFloat(name, value);
    }
}
