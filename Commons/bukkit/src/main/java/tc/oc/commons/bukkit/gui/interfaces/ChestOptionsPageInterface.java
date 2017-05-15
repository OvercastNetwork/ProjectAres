package tc.oc.commons.bukkit.gui.interfaces;

import tc.oc.commons.bukkit.gui.Interface;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.gui.buttons.empty.EmptyButton;
import tc.oc.commons.bukkit.gui.buttons.toggle.ToggleButton;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.commons.bukkit.util.ItemCreator;
import tc.oc.commons.bukkit.util.ObjectUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * MultiPageInterfaces are a special group of interfaces. They aren't a single interface, but a collection of
 * SinglePageInterfaces. Given a set of buttons, it will add them to a page with default items in order. This can be
 * used as a way to provide a list of non-hardcoded items without having to define their slots.
 */

public class ChestOptionsPageInterface extends SinglePageInterface {

    public ChestOptionsPageInterface(List<Button> buttons, int size, String title, Interface parent) {
        this(null, buttons, size, title, parent, 1);
    }

    public ChestOptionsPageInterface(List<Button> buttons, int size, String title, Interface parent, int i) {
        this(null, buttons, size, title, parent, i);
    }

    public ChestOptionsPageInterface(Player player, List<Button> buttons, int size, String title, Interface parent) {
        this(player, buttons, size, title, parent, 1);
    }

    public ChestOptionsPageInterface(Player player, List<Button> buttons, int size, String title, Interface parent, int i, Object... data) {
        super(player, buttons, size, title + (i > 1 ? " - " + i : ""), parent, 1, data);
    }

    @Override
    public void updateButtons() {
        if (getButtons().size() == 0) {
            List<Button> buttons = new ArrayList<>();
            for (Button button : getDefaultButtons()) {
                button.setSlot(getNextSlot(button.getSlot(), buttons));
                if (button.getSlot() < getSize()) {
                    buttons.add(button);
                }
            }
            Button empty = new Button(
                    new ItemCreator(Material.DEAD_BUSH)
                            .setName(Constants.PREFIX + "Nothing here..."));
            empty.setSlot(getNextSlot(empty.getSlot(), buttons));
            if (empty.getSlot() < getSize()) {
                buttons.add(empty);
            }
            buttons.remove(this.nextPageButton);
            setButtons(buttons);
            updateInventory();
            return;
        }
        int allButtons = getDefaultButtons().size() + getButtons().size()*2;
        //This tells the plugin how many pages are needed to store all of the items.
        int allPages = (int)((double) ((allButtons) / (getSize() - getDefaultButtons().size())));
        if (((getButtons().size() + 1) % (getSize() - getDefaultButtons().size()) == 0) && allPages > 1) {
            allPages = allPages - 1;
        }
        if (allButtons < getSize()) {
            allPages = 1;
        }
        if (allPages >= 3) {
            //allPages = allPages - 1;
        }
        //This gets the items for the page it is currently on.
        List<Button> buttons = ObjectUtils.paginate(getButtons(), page, (getSize() - getDefaultButtons().size())/2);
        try {
            if (buttons.size() != 0) {
                ArrayList<Button> currentButtons = new ArrayList<>();
                for (Button button : getDefaultButtons()) {
                    if (button.equals(this.lastPageButton)) {
                        button.setIcon(button.getIcon().setSize(page - 1));
                    } else if (button.equals(this.nextPageButton)) {
                        button.setIcon(button.getIcon().setSize(page + 1));
                    }
                    button.setSlot(button.getSlot());
                    if (button.getSlot() > getSize()) {
                        break;
                    }
                    currentButtons.add(button);
                }
                int currentButton = 0;
                for (Button button : buttons) {
                    if (buttons.indexOf(button) > 0 && buttons.get(buttons.size() - 1).equals(button) && page == (allPages)) {
                        //      break;
                    }
                    final ToggleButton toggleButton = (ToggleButton) button;
                    toggleButton.setSlot(getNextSlot(currentButton, currentButtons));
                    ItemCreator dye = new ItemCreator(Material.INK_SACK)
                            .setData(toggleButton.getState() == true ? 10 : 8)
                            .setName((toggleButton.getState() == true ? ChatColor.GREEN : ChatColor.RED) + ChatColor.BOLD.toString() + (toggleButton.getState() == true ? "Enabled" : "Disabled"));
                    Button toggleDye = new Button(dye) {
                        @Override
                        public void function(Player player) {
                            toggleButton.function(player);
                        }
                    };
                    toggleDye.setSlot(toggleButton.getSlot() + 9);
                    if (button.getSlot() > getSize()) {
                        break;
                    }
                    currentButtons.add(toggleButton);
                    currentButtons.add(toggleDye);
                    currentButton = toggleButton.getSlot();
                }
                if (allPages == page) {
                    currentButtons.remove(this.nextPageButton);
                }
                setButtons(currentButtons);
                updateInventory();
            }

        } catch (Exception e) {
            if (page > 0) {
                page = page - 1;
                updateButtons();
            }
        }

    }

    @Override
    public void setDefaultButtons() {
        defaultButtons.clear();
        for (Integer integer : new Integer[]{1, 2, 3, 4, 5, 6, 7, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 11, 20, 29, 38, 13, 22, 31, 40, 15, 24, 33, 42}) {
            if (integer > getSize()) {
                break;
            }
            EmptyButton button = new EmptyButton(integer);
            defaultButtons.add(button);
        }
        defaultButtons.add(this.lastPageButton);
        defaultButtons.add(this.nextPageButton);
    }

}
