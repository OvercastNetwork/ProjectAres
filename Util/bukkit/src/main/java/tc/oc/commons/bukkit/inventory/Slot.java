package tc.oc.commons.bukkit.inventory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.commons.bukkit.item.ItemUtils;
import tc.oc.commons.core.util.Streams;

/**
 * Derived from the names found in {@link net.minecraft.server.CommandReplaceItem}.
 * If we ever implement applying kits to other types of inventories, this should be
 * expanded to include those slot names as well.
 */
public abstract class Slot<I extends Inventory, H extends InventoryHolder> {
    static {
        all = new HashSet<>();
        byKey = new HashMap<>();
        byIndex = HashBasedTable.create();
        byInventoryType = ImmutableMap.<Class<? extends Inventory>, Class<? extends Slot>>builder()
                                      .put(PlayerInventory.class, Player.class)
                                      .put(Inventory.class, Container.class)
                                      .build();
        Container.init();
        Player.init();
        EnderChest.init();
    }

    private static final Set<Slot> all;
    private static final Map<String, Slot> byKey;
    private static final Table<Class<? extends Slot>, Integer, Slot> byIndex;
    private static final Map<Class<? extends Inventory>, Class<? extends Slot>> byInventoryType;

    /**
     * Convert a Mojang slot name (used by /replaceitem) to a {@link Slot} object.
     * The "slot." at the beginning of the name is optional.
     * Returns null if the name is invalid.
     */
    public static @Nullable Slot forKey(String key) {
        if(key.startsWith("slot.")) {
            key = key.substring("slot.".length());
        }
        return byKey.get(key);
    }

    public static @Nullable <S extends Slot> S forIndex(Class<S> type, int index) {
        return (S) byIndex.get(type, index);
    }

    public static @Nullable <S extends Slot> S atPosition(Class<S> type, int column, int row) {
        return forIndex(type, row * 9 + column);
    }

    public static <I extends Inventory> Class<? extends Slot<I, ?>> typeForInventory(Class<I> inv) {
        for(Map.Entry<Class<? extends Inventory>, Class<? extends Slot>> entry : byInventoryType.entrySet()) {
            if(entry.getKey().isAssignableFrom(inv)) {
                return (Class<? extends Slot<I, ?>>) entry.getValue();
            }
        }
        throw new IllegalStateException("Weird inventory type " + inv);
    }

    public static <I extends Inventory> Stream<? extends Slot<I, ?>> forInventory(Class<I> invType) {
        return Streams.instancesOf(all.stream(), typeForInventory(invType));
    }

    public static @Nullable <I extends Inventory> Slot<I, ?> forInventoryIndex(Class<I> inv, int index) {
        return forIndex(typeForInventory(inv), index);
    }

    public static @Nullable Slot<?, ?> forViewIndex(InventoryView view, int rawIndex) {
        final int cookedIndex = view.convertSlot(rawIndex);
        return forInventoryIndex((rawIndex == cookedIndex ? view.getTopInventory()
                                                          : view.getBottomInventory()).getClass(),
                                 cookedIndex);
    }

    private final @Nullable String key;
    private final int index; // -1 = no slot

    Slot(Class<? extends Slot> type, @Nullable String key, int index) {
        this.key = key;
        this.index = index;

        all.add(this);

        if(key != null) {
            byKey.put(key, this);
        }

        if(index >= 0) {
            byIndex.put(type, index, this);
        }
    }

    @Override
    public String toString() {
        return key != null ? getKey() : super.toString();
    }

    /**
     * @return the name of this slot, as used by the /replaceitem command
     */
    public @Nullable String getKey() {
        return key == null ? null : "slot." + key;
    }

    public boolean hasIndex() {
        return index >= 0;
    }

    /**
     * @return a slot index that can be passed to {@link Inventory#getItem} et al.
     */
    public int getIndex() {
        if(!hasIndex()) {
            throw new UnsupportedOperationException("Slot " + this + " has no index");
        }
        return index;
    }

    public int getColumn() {
        return getIndex() % 9;
    }

    public int getRow() {
        return getIndex() / 9;
    }

    public int maxStackSize() {
        return 64;
    }

    public int maxStackSize(Material material) {
        return Math.min(maxStackSize(), material.getMaxStackSize());
    }

    public int maxStackSize(ItemStack item) {
        return maxStackSize(item.getType());
    }

    public int maxTransferrableIn(ItemStack source) {
        return Math.min(source.getAmount(), maxStackSize(source));
    }

    public int maxTransferrableIn(ItemStack source, I inv) {
        final ItemStack dest = getItem(inv);
        if(ItemUtils.isNothing(dest)) {
            return maxTransferrableIn(source);
        } else if(dest.isSimilar(source)) {
            return Math.min(source.getAmount(), Math.max(0, maxStackSize(dest) - dest.getAmount()));
        } else {
            return 0;
        }
    }

    public boolean isEquipment() {
        return false;
    }

    public EquipmentSlot toEquipmentSlot() {
        throw new UnsupportedOperationException("Slot " + this + " is not an equipment slot");
    }

    public I getInventory(H holder) {
        return (I) holder.getInventory();
    }

    protected static @Nullable ItemStack airToNull(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR ? null : stack;
    }

    public @Nullable ItemStack getItem(H holder) {
        return getItem(getInventory(holder));
    }

    public Optional<ItemStack> item(H holder) {
        return Optional.ofNullable(getItem(holder));
    }

    public void putItem(H holder, ItemStack stack) {
        putItem(getInventory(holder), stack);
    }

    /**
     * @return the item in this slot of the given holder's inventory, or null if the slot is empty.
     *         This will never return a stack of {@link Material#AIR}.
     */
    public @Nullable ItemStack getItem(I inv) {
        return airToNull(inv.getItem(getIndex()));
    }

    public Optional<ItemStack> item(I inv) {
        return Optional.ofNullable(getItem(inv));
    }

    public int amount(I inv) {
        return ItemUtils.amount(getItem(inv));
    }

    public boolean isEmpty(I inv) {
        return amount(inv) == 0;
    }

    /**
     * Put the given stack in this slot of the given holder's inventory.
     */
    public void putItem(I inv, ItemStack stack) {
        inv.setItem(getIndex(), airToNull(stack));
    }

    public static class Container extends Slot<Inventory, InventoryHolder> {
        static void init() {
            for(int i = 0; i < 54; i++) {
                new Container("container." + i, i);
            }
        }

        public static @Nullable Container forIndex(int index) {
            return forIndex(Container.class, index);
        }

        Container(String key, int index) {
            super(Container.class, key, index);
        }
    }

    public static abstract class Player extends Slot<PlayerInventory, org.bukkit.entity.Player> {
        static void init() {
            Storage.init();
            Equipment.init();
            Cursor.init();
        }

        public static Stream<Player> player() {
            return Streams.concat(Storage.storage(),
                                  Equipment.equipment(),
                                  Stream.of(Cursor.cursor()));
        }

        Player(String key, int index) {
            super(Player.class, key, index);
        }

        public static @Nullable Player forIndex(int index) {
            return forIndex(Player.class, index);
        }

        @Override
        public @Nullable ItemStack getItem(PlayerInventory inv) {
            return isEquipment() ? airToNull(inv.getItem(toEquipmentSlot()))
                                 : super.getItem(inv);

        }

        @Override
        public void putItem(PlayerInventory inv, ItemStack stack) {
            if(isEquipment()) {
                inv.setItem(toEquipmentSlot(), stack);
            } else {
                super.putItem(inv, stack);
            }
        }
    }

    public static class Storage extends Player {
        static void init() {
            Hotbar.init();
            Pockets.init();
        }

        public static Stream<? extends Storage> storage() {
            return Stream.concat(Hotbar.hotbar(), Pockets.pockets());
        }

        Storage(String key, int index) {
            super(key, index);
        }
    }

    // TODO: make row/column correct for Hotbar and Pockets

    public static class Hotbar extends Storage {
        static void init() {
            hotbar = new Hotbar[9];
            for(int i = 0; i < 9; i++) {
                hotbar[i] = new Hotbar("hotbar." + i, i);
            }
        }

        private static Hotbar[] hotbar;
        public static Stream<? extends Hotbar> hotbar() {
            return Stream.of(hotbar);
        }

        public static Hotbar forPosition(int pos) {
            return (Hotbar) forIndex(pos);
        }

        protected Hotbar(String key, int index) {
            super(key, index);
        }
    }

    public static class Pockets extends Storage {
        static void init() {
            pockets = new Pockets[27];
            for(int i = 0; i < 27; i++) {
                pockets[i] = new Pockets("inventory." + i, 9 + i);
            }
            MainHand.init();
        }

        private static Pockets[] pockets;
        public static Stream<? extends Pockets> pockets() {
            return Stream.of(pockets);
        }

        protected Pockets(String key, int index) {
            super(key, index);
        }
    }

    public abstract static class Equipment extends Player {
        static void init() {
            OffHand.init();
            Armor.init();
        }

        public static Stream<? extends Equipment> equipment() {
            return Stream.concat(Stream.of(OffHand.offHand()), Armor.armor());
        }

        private final EquipmentSlot equipmentSlot;

        Equipment(String key, int index, EquipmentSlot equipmentSlot) {
            super(key, index);
            this.equipmentSlot = equipmentSlot;
        }

        @Override
        public int maxStackSize() {
            return 1;
        }

        @Override
        public boolean isEquipment() {
            return true;
        }

        @Override
        public EquipmentSlot toEquipmentSlot() {
            return equipmentSlot;
        }
    }

    public static class MainHand extends Hotbar {
        static void init() {
            mainHand = new MainHand();
        }

        private static MainHand mainHand;
        public static MainHand mainHand() { return mainHand; }

        MainHand() {
            super("weapon.mainhand", -1);
        }

        @Override
        public boolean isEquipment() {
            return true;
        }

        @Override
        public EquipmentSlot toEquipmentSlot() {
            return EquipmentSlot.HAND;
        }
    }

    public static class OffHand extends Equipment {
        static void init() {
            offHand = new OffHand();
        }

        private static OffHand offHand;
        public static OffHand offHand() { return offHand; }

        protected OffHand() {
            super("weapon.offhand", 40, EquipmentSlot.OFF_HAND);
        }
    }

    public static class Armor extends Equipment {
        static void init() {
            new Armor("armor.feet",  EquipmentSlot.FEET, ArmorType.BOOTS);
            new Armor("armor.legs",  EquipmentSlot.LEGS, ArmorType.LEGGINGS);
            new Armor("armor.chest", EquipmentSlot.CHEST, ArmorType.CHESTPLATE);
            new Armor("armor.head",  EquipmentSlot.HEAD, ArmorType.HELMET);
        }

        private static final Map<ArmorType, Armor> byArmorType = new EnumMap<>(ArmorType.class);

        public static Stream<? extends Armor> armor() {
            return byArmorType.values().stream();
        }

        private final ArmorType armorType;

        Armor(String key, EquipmentSlot equipmentSlot, ArmorType armorType) {
            super(key, armorType.inventorySlot(), equipmentSlot);
            this.armorType = armorType;
            byArmorType.put(armorType, this);
        }

        public ArmorType getArmorType() {
            return armorType;
        }

        public static Armor forType(ArmorType armorType) {
            return byArmorType.get(armorType);
        }
    }

    public static class EnderChest extends Slot<Inventory, org.bukkit.entity.Player> {
        static void init() {
            for(int i = 0; i < 27; i++) {
                new EnderChest("enderchest." + i, i);
            }
        }

        EnderChest(String key, int index) {
            super(EnderChest.class, key, index);
        }

        @Override
        public Inventory getInventory(org.bukkit.entity.Player holder) {
            return holder.getEnderChest();
        }
    }

    public static class Cursor extends Player {
        static void init() {
            cursor = new Cursor();
        }

        private static Cursor cursor;
        public static Cursor cursor() { return cursor; }

        Cursor() {
            super(null, -1);
        }

        @Override
        public String toString() {
            return "cursor";
        }

        @Override
        public @Nullable ItemStack getItem(org.bukkit.entity.Player holder) {
            return airToNull(holder.getItemOnCursor());
        }

        @Override
        public void putItem(org.bukkit.entity.Player holder, @Nullable ItemStack stack) {
            holder.setItemOnCursor(stack);
        }

        @Override
        public @Nullable ItemStack getItem(PlayerInventory inv) {
            return getItem((org.bukkit.entity.Player) inv.getHolder());
        }

        @Override
        public void putItem(PlayerInventory inv, ItemStack stack) {
            putItem((org.bukkit.entity.Player) inv.getHolder(), stack);
        }
    }
}
