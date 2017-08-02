package tc.oc.commons.bukkit.item;

import java.util.Objects;

import com.google.common.base.Preconditions;
import net.minecraft.server.NBTTagCompound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tc.oc.commons.bukkit.util.NBTUtils;

import javax.annotation.Nullable;

public abstract class ItemTag<T> {

    protected final String name;
    protected final T defaultValue;

    protected ItemTag(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    protected abstract boolean hasPrimitive(NBTTagCompound tag);

    protected abstract T getPrimitive(NBTTagCompound tag);

    protected abstract void setPrimitive(NBTTagCompound tag, T value);

    protected void clearPrimitive(NBTTagCompound tag) {
        tag.remove(name);
    }

    public boolean has(@Nullable NBTTagCompound tag) {
        return tag != null && hasPrimitive(tag);
    }

    public boolean has(@Nullable ItemMeta meta) {
        return has(NBTUtils.getCustomTag(meta));
    }

    public boolean has(@Nullable ItemStack stack) {
        return has(NBTUtils.getCustomTag(stack));
    }

    public T get(@Nullable NBTTagCompound tag) {
        if(tag != null && hasPrimitive(tag)) {
            return getPrimitive(tag);
        } else {
            return defaultValue;
        }
    }

    public T get(@Nullable ItemMeta meta) {
        return get(NBTUtils.getCustomTag(meta));
    }

    public T get(@Nullable ItemStack stack) {
        return get(NBTUtils.getCustomTag(stack));
    }

    public void set(NBTTagCompound tag, T value) {
        if(Objects.equals(value, defaultValue)) {
            clear(tag);
        } else {
            setPrimitive(tag, Preconditions.checkNotNull(value));
        }
    }

    public void set(ItemMeta meta, T value) {
        set(NBTUtils.getOrCreateCustomTag(meta), value);
    }

    public void set(ItemStack stack, T value) {
        ItemUtils.updateMeta(stack, meta -> set(meta, value));
    }

    public void clear(@Nullable NBTTagCompound tag) {
        if(tag != null) clearPrimitive(tag);
    }

    public void clear(@Nullable ItemMeta meta) {
        clear(NBTUtils.getCustomTag(meta));
        NBTUtils.prune(meta);
    }

    public void clear(@Nullable ItemStack stack) {
        ItemUtils.updateMetaIfPresent(stack, this::clear);
    }
}
