package tc.oc.commons.bukkit.gui.buttons.empty;

import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.util.ItemCreator;
import org.bukkit.Material;

public class EmptyButton extends Button {

    public EmptyButton(int slot) {
        super(new ItemCreator(Material.AIR), slot);
    }

}
