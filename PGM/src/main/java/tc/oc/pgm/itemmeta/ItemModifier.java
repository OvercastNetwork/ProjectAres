package tc.oc.pgm.itemmeta;

import java.util.Optional;
import javax.inject.Inject;

import org.bukkit.inventory.ItemStack;

public class ItemModifier {

    private final Optional<ItemModifyModule> module;

    @Inject ItemModifier(Optional<ItemModifyModule> module) {
        this.module = module;
    }

    public boolean needsModification(ItemStack stack) {
        return module.isPresent() && module.get().shouldApply(stack);
    }

    public ItemStack modifyCopy(ItemStack stack) {
        if(needsModification(stack)) {
            stack = module.get().applyToCopy(stack);
        }
        return stack;
    }

    public ItemStack modify(ItemStack stack) {
        if(needsModification(stack)) {
            module.get().applyRules(stack);
        }
        return stack;
    }
}
