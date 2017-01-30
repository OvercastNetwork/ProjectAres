package tc.oc.commons.bukkit.util;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.CraftBukkitRuntime;
import org.bukkit.Material;
import org.bukkit.craftbukkit.potion.CraftPotionBrewer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.bukkit.potion.PotionEffectType.*;
import static org.junit.Assert.*;
import static tc.oc.commons.bukkit.util.PotionClassification.*;

/** Tests for {@link PotionUtils} and {@link PotionClassification} */
@RunWith(JUnit4.class)
public class PotionClassificationTest {

    @Before
    public void setUp() {
        CraftBukkitRuntime.load();
        if(Potion.getBrewer() == null) {
            Potion.setPotionBrewer(new CraftPotionBrewer());
        }
    }

    @Test
    public void effectTypes() throws Exception {
        assertEquals(BENEFICIAL, classify(HEAL));
        assertEquals(HARMFUL, classify(HARM));
    }

    @Test
    public void classifyByMostEffects() throws Exception {
        assertEquals(BENEFICIAL, classify(ImmutableList.of(
            new PotionEffect(SPEED, 1, 0),
            new PotionEffect(HARM, 1, 0),
            new PotionEffect(LUCK, 1, 0)
        )));

        assertEquals(HARMFUL, classify(ImmutableList.of(
            new PotionEffect(SLOW, 1, 0),
            new PotionEffect(HEAL, 1, 0),
            new PotionEffect(UNLUCK, 1, 0)
        )));
    }

    @Test
    public void classifyByDuration() throws Exception {
        assertEquals(BENEFICIAL, classify(ImmutableList.of(
            new PotionEffect(HEAL, 2, 0),
            new PotionEffect(HARM, 1, 0)
        )));

        assertEquals(HARMFUL, classify(ImmutableList.of(
            new PotionEffect(HEAL, 1, 0),
            new PotionEffect(HARM, 2, 0)
        )));
    }

    @Test
    public void classifyByAmplifier() throws Exception {
        assertEquals(BENEFICIAL, classify(ImmutableList.of(
            new PotionEffect(HEAL, 1, 1),
            new PotionEffect(HARM, 1, 0)
        )));

        assertEquals(HARMFUL, classify(ImmutableList.of(
            new PotionEffect(HEAL, 1, 0),
            new PotionEffect(HARM, 1, 1)
        )));
    }

    @Test
    public void negativeAmplifier() throws Exception {
        assertEquals(BENEFICIAL, classify(ImmutableList.of(
            new PotionEffect(HARM, 1, -1)
        )));
        assertEquals(HARMFUL, classify(ImmutableList.of(
            new PotionEffect(HEAL, 1, -1)
        )));
    }

    @Test
    public void vanillaBrews() throws Exception {
        assertEquals(BENEFICIAL, classify(Bukkit.potionRegistry().get(Bukkit.key("healing"))));
        assertEquals(BENEFICIAL, classify(new PotionData(PotionType.INSTANT_HEAL, false, false)));
        assertEquals(HARMFUL, classify(Bukkit.potionRegistry().get(Bukkit.key("harming"))));
        assertEquals(HARMFUL, classify(new PotionData(PotionType.INSTANT_DAMAGE, false, false)));
    }

    @Test
    public void potionItem() throws Exception {
        final ItemStack item = new ItemStack(Material.POTION);
        final PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setPotionBrew(Bukkit.potionRegistry().get(Bukkit.key("healing")));
        item.setItemMeta(meta);

    }

    @Test
    public void riftCaseTest() {
        List<PotionEffect> effects = ImmutableList.of(
                new PotionEffect(FAST_DIGGING, 3600, 3),
                new PotionEffect(REGENERATION, 3600, 2),
                new PotionEffect(DAMAGE_RESISTANCE, 3600, 1),
                new PotionEffect(FIRE_RESISTANCE, 3600, 1),
                new PotionEffect(SPEED, 3600, 1),
                new PotionEffect(INCREASE_DAMAGE, 3600, 1)
        );

        assertEquals("Rift Baron potion was not classified as <BENEFICIAL>",
                     PotionClassification.BENEFICIAL, classify(effects));
    }
}
