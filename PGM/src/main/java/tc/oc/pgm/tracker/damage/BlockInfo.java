package tc.oc.pgm.tracker.damage;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.pgm.match.ParticipantState;

public class BlockInfo extends OwnerInfoBase implements PhysicalInfo {

    @Inspect private final MaterialData material;

    public BlockInfo(MaterialData material, @Nullable ParticipantState owner) {
        super(owner);
        this.material = material;
    }

    public BlockInfo(MaterialData material) {
        this(material, null);
    }

    public BlockInfo(BlockState block, @Nullable ParticipantState owner) {
        this(block.getMaterialData(), owner);
    }

    public BlockInfo(BlockState block) {
        this(block, null);
    }

    public MaterialData getMaterial() {
        return material;
    }

    @Override
    public String getIdentifier() {
        return getMaterial().getItemType().name();
    }

    @Override
    public BaseComponent getLocalizedName() {
        String key = NMSHacks.getTranslationKey(getMaterial());
        return key != null ? new TranslatableComponent(key)
                           : new Component(getMaterial().getItemType().name());
    }
}
