package tc.oc.commons.bukkit.localization;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.util.Optionals;

public class BukkitTranslatorImpl implements BukkitTranslator {

    private enum Substance {
        GRASS("tile.tallgrass.name"),
        COAL("item.coal.name"),
        BRICK("item.brick.name"),
        IRON("material.iron"),
        QUARTZ("material.quartz"),
        GOLD("material.gold"),
        DIAMOND("item.diamond.name"),
        EMERALD("item.emerald.name"),
        REDSTONE("item.redstone.name"),
        ;

        final String key;
        String key() { return key; }

        Substance(String key) {
            this.key = key;
        }
    }

    private final Translator translator;

    @Inject BukkitTranslatorImpl(Translator translator) {
        this.translator = translator;
    }

    private @Nullable Substance substance(Material material) {
        switch(material) {
            case GRASS:
                return Substance.GRASS;

            case COAL_BLOCK:
                return Substance.COAL;

            case BRICK:
            case CLAY_BRICK:
                return Substance.BRICK;

            case IRON_NUGGET:
            case IRON_INGOT:
            case IRON_BLOCK:
                return Substance.IRON;

            case QUARTZ:
            case QUARTZ_BLOCK:
                return Substance.QUARTZ;

            case GOLD_NUGGET:
            case GOLD_INGOT:
            case GOLD_BLOCK:
                return Substance.GOLD;

            case DIAMOND:
            case DIAMOND_BLOCK:
                return Substance.DIAMOND;

            case EMERALD:
            case EMERALD_BLOCK:
                return Substance.EMERALD;

            case REDSTONE:
            case REDSTONE_BLOCK:
            case REDSTONE_WIRE:
                return Substance.REDSTONE;

            default:
                return null;
        }
    }

    @Override
    public Optional<String> materialKey(Material material) {
        return Optionals.first(
            Optional.ofNullable(substance(material))
                    .map(Substance::key),
            Optional.ofNullable(NMSHacks.getTranslationKey(material))
        );
    }

    @Override
    public Optional<String> materialKey(MaterialData material) {
        return Optionals.first(
            Optional.ofNullable(substance(material.getItemType()))
                    .map(Substance::key),
            Optional.ofNullable(NMSHacks.getTranslationKey(material))
        );
    }
}
