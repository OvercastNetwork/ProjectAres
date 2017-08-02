package tc.oc.pgm.damage;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.BooleanType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import tc.oc.commons.bukkit.util.ItemCreator;

public class DamageSettings {

    public static final Setting DAMAGE_NUMBERS = new SettingBuilder()
        .name("DamageNumbers").alias("damage")
        .summary("Show floating damage numbers")
        .type(new BooleanType())
        .defaultValue(true)
        .get();

    public static final Setting KNOCKBACK_PARTICLES = new SettingBuilder()
        .name("KnockbackParticles").alias("kbparticles")
        .summary("Show knockback particles")
        .type(new BooleanType())
        .defaultValue(true)
        .get();

    public static final Setting ATTACK_SPEEDOMETER = new SettingBuilder()
        .name("AttackSpeedometer").alias("cps")
        .summary("Show attack speed information")
        .type(new BooleanType())
        .defaultValue(false).get();
}
