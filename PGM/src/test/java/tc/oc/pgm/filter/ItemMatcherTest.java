package tc.oc.pgm.filter;

import org.bukkit.CraftBukkitRuntime;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ImItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.Wool;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.junit.Before;
import org.junit.Test;
import tc.oc.pgm.filters.ItemMatcher;
import tc.oc.pgm.kits.tag.ItemTags;
import tc.oc.pgm.projectile.Projectiles;

import static org.junit.Assert.*;

public class ItemMatcherTest {

    @Before
    public void setUp() throws Exception {
        CraftBukkitRuntime.load();
    }

    private void assertMatches(ItemStack item, ItemStack query) {
        final ItemMatcher matcher = new ItemMatcher(item);

        if(!matcher.test(query)) {
            fail("Item should match: reference=" + item + " query=" + query);
        }

        final CraftItemStack nms = CraftItemStack.asCraftMirror(CraftItemStack.asNMSCopy(query));
        if(!matcher.test(nms)) {
            fail("Converted item should match: reference=" + item + " query=" + query);
        }
    }

    private void refuteMatches(ItemStack item, ItemStack query) {
        final ItemMatcher matcher = new ItemMatcher(item);

        if(matcher.test(query)) {
            fail("Item should not match: reference=" + item + " query=" + query);
        }

        final CraftItemStack nms = CraftItemStack.asCraftMirror(CraftItemStack.asNMSCopy(query));
        if(matcher.test(nms)) {
            fail("Converted item should not match: reference=" + item + " query=" + query);
        }
    }
    
    @Test
    public void simpleItemMatches() throws Throwable {
        ImItemStack item = ItemStack.builder(Material.BEDROCK)
                                    .immutable();
        assertMatches(item, item);
    }

    @Test
    public void itemWithDataMatches() throws Throwable {
        ImItemStack item = ItemStack.builder(new Wool(DyeColor.PINK))
                                    .immutable();
        assertMatches(item, item);
    }

    @Test
    public void itemWithMetaMatches() throws Throwable {
        ImItemStack item = ItemStack.builder(Material.BEDROCK)
                                    .meta(meta -> meta.setDisplayName("Hi!"))
                                    .immutable();
        assertMatches(item, item);
    }

    @Test
    public void itemWithTypedMetaMatches() throws Throwable {
        ImItemStack item = ItemStack.builder(Material.POTION)
                                    .meta(PotionMeta.class, meta -> meta.setBasePotionData(new PotionData(PotionType.LUCK, false, false)))
                                    .immutable();
        assertMatches(item, item);
    }

    @Test
    public void itemWithCustomProjectileMatches() throws Throwable {
        ImItemStack item = ItemStack.builder(Material.STICK)
                                    .meta(meta -> Projectiles.setProjectileId(meta, "woot"))
                                    .immutable();
        assertMatches(item, item);
    }

    @Test
    public void differentMaterialsDontMatch() throws Throwable {
        refuteMatches(new ItemStack(Material.BEDROCK),
                      new ItemStack(Material.APPLE));
    }

    @Test
    public void differentDataDoesntMatch() throws Throwable {
        refuteMatches(new ItemStack(new Wool(DyeColor.PINK)),
                      new ItemStack(new Wool(DyeColor.BLUE)));
    }

    @Test
    public void differentMetaDoesntMatch() throws Throwable {
        refuteMatches(ItemStack.builder(Material.BEDROCK).meta(meta -> meta.setDisplayName("Hi!")).immutable(),
                      ItemStack.builder(Material.BEDROCK).meta(meta -> meta.setDisplayName("Bye!")).immutable());
    }

    @Test
    public void differentProjectileDoesntMatch() throws Throwable {
        refuteMatches(ItemStack.builder(Material.STICK).meta(meta -> Projectiles.setProjectileId(meta, "woot")).immutable(),
                      ItemStack.builder(Material.STICK).meta(meta -> Projectiles.setProjectileId(meta, "doink")).immutable());
    }

    @Test
    public void nullDoesntMatch() throws Throwable {
        assertFalse(new ItemMatcher(new ItemStack(Material.BEDROCK)).test(null));
    }

    @Test
    public void biggerStackMatches() throws Throwable {
        assertMatches(new ItemStack(Material.BEDROCK, 3),
                      new ItemStack(Material.BEDROCK, 4));
    }

    @Test
    public void smallerStackDoesNotMatch() throws Throwable {
        refuteMatches(new ItemStack(Material.BEDROCK, 3),
                      new ItemStack(Material.BEDROCK, 2));
    }

    @Test
    public void durabilityIgnoredOnDamageableItem() throws Throwable {
        assertMatches(new ItemStack(Material.STONE_SWORD, 1, (short) 123),
                      new ItemStack(Material.STONE_SWORD, 1, (short) 456));
    }

    @Test
    public void durabilityMattersOnDataItem() throws Throwable {
        refuteMatches(new ItemStack(Material.WOOL, 1, (short) 1),
                      new ItemStack(Material.WOOL, 1, (short) 2));
    }

    @Test
    public void lockFlagIgnored() throws Throwable {
        ItemStack ref = new ItemStack(Material.BEDROCK);
        ItemStack query = ref.clone();
        ItemTags.LOCKED.set(query, true);
        assertMatches(ref, query);
    }

    @Test
    public void preventSharingFlagIgnored() throws Throwable {
        ItemStack ref = new ItemStack(Material.BEDROCK);
        ItemStack query = ref.clone();
        ItemTags.PREVENT_SHARING.set(query, true);
        assertMatches(ref, query);
    }
}
