package tc.oc.pgm.tracker.damage;

import java.util.Optional;
import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.pgm.match.ParticipantState;

public class FallingBlockInfo extends EntityInfo implements DamageInfo {

    @Inspect private final Material material;

    public FallingBlockInfo(FallingBlock entity, @Nullable ParticipantState owner) {
        super(entity, owner);
        this.material = entity.getMaterial();
    }

    public FallingBlockInfo(FallingBlock entity) {
        this(entity, null);
    }

    @Override
    public Optional<PhysicalInfo> damager() {
        return Optional.of(this);
    }

    @Override
    public @Nullable ParticipantState getAttacker() {
        return getOwner();
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public BaseComponent getLocalizedName() {
        return new TranslatableComponent(NMSHacks.getTranslationKey(getMaterial()));
    }
}
