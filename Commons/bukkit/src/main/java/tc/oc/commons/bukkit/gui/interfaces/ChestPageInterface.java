package tc.oc.commons.bukkit.gui.interfaces;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import tc.oc.commons.bukkit.gui.Interface;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.gui.buttons.empty.EmptyButton;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.commons.bukkit.util.ItemCreator;
import tc.oc.commons.bukkit.util.ObjectUtils;

/**
 * MultiPageInterfaces are a special group of interfaces. They aren't a single interface, but a collection of
 * SinglePageInterfaces. Given a set of buttons, it will add them to a page with default items in order. This can be
 * used as a way to provide a list of non-hardcoded items without having to define their slots.
 */

@Deprecated
public class ChestPageInterface extends ChestInterface {

    private List<ChestInterface> pages = new ArrayList<>();

   // public final LastPageButton lastPageButton = new LastPageButton(this, 0); TODO
    //public final NextPageButton nextPageButton = new NextPageButton(this, 8);

    public ChestPageInterface(List<Button> buttons, int size, String title, Interface parent) {
        this(null, buttons, size, title, parent);
    }

    public ChestPageInterface(Player player, List<Button> buttons, int size, String title, Object... data) {
        super(player, buttons, size, title);
        /*
        MultiPageInterfaces must be contain necessary default items, if it cannot contain the next page item
        (currently has the highest slot value of a necessary default item), it won't allow proper navagation.
         */
        setData(data);
        //setSize((size >= this.nextPageButton.getSlot() + 1 ? size : this.nextPageButton.getSlot() + 1));
    }

    public void setupInventory() {
        pages.clear();
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
            pages.add(new ChestInterface(null, buttons, getSize(), getTitle()));
            return;
        }
        int allButtons = getDefaultButtons().size() + getButtons().size();
        //This tells the plugin how many pages are needed to store all of the items.
        int allPages = (int) Math.round((double) (allButtons) / (getSize() - getDefaultButtons().size()));
        if (((getButtons().size() + 1) % (getSize() - getDefaultButtons().size()) == 0) && allPages > 1) {
            allPages = allPages - 1;
        }
        if (allPages >= 3) {
            allPages = allPages - 1;
        }
        for (int i = 1; i <= allPages; i++) {
            //This gets the items for the page it is currently on.
            List<Button> buttons = ObjectUtils.paginate(getButtons(), i, getSize() - getDefaultButtons().size());
            if (allButtons < getSize()) {
                allPages = 1;
            }
            if (buttons.size() != 0) {
                ArrayList<Button> currentButtons = new ArrayList<>();
                for (Button button : getDefaultButtons()) {
                    button.setSlot(button.getSlot());
                    if (button.getSlot() > getSize()) {
                        break;
                    }
                    currentButtons.add(button);
                }
                int currentButton = 0;
                for (Button button : buttons) {
                    button.setSlot(getNextSlot(currentButton, currentButtons));
                    if (button.getSlot() > getSize()) {
                        break;
                    }
                    currentButtons.add(button);
                    currentButton++;
                }
                if (allPages == i) {
                 //   currentButtons.remove(this.nextPageButton);
                }
                String suffix = i > 1 ? " - " + i : "";
                ChestInterface gui = new ChestInterface(null, currentButtons, getSize(), getTitle() + suffix);
                gui.updateInventory();
                pages.add(gui);
            }
        }
    }

    public void setPages(List<ChestInterface> pages) {
        this.pages = pages;
    }

    public List<ChestInterface> getPages() {
        return this.pages;
    }

    public int getNextSlot(int slot, List<Button> buttons) {
        for (Button button : buttons) {
            if (button.getSlot() == slot) {
                return getNextSlot(slot + 1, buttons);
            }
        }
        return slot;
    }

    /*

        Inventory guide.
        00 01 02 03 04 05 06 07 08
        09 10 11 12 13 14 15 16 17
        18 19 20 21 22 23 24 25 26
        27 28 29 30 31 32 33 34 35
        36 37 38 39 40 41 42 43 44
        45 46 47 48 49 50 51 52 53

        Key:
        p = Page arrow
        x = Empty space
        o = Paginated item

        Current inventory layout
        p x x x x x x x p
        x o o o o o o o x
        x o o o o o o o x
        x o o o o o o o x
        x o o o o o o o x
        x x x x x x x x x

        Everything is easily customizable. The methods take currently taken slots into consideration.

    */
    public List<Button> getDefaultButtons() {
        List<Button> buttons = new ArrayList<>();
        for (Integer integer : new Integer[]{1, 2, 3, 4, 5, 6, 7, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53}) {
            if (integer > getSize()) {
                break;
            }
            EmptyButton button = new EmptyButton(integer);
            buttons.add(button);
        }
   //buttons.add(this.lastPageButton); TODO
       // buttons.add(this.nextPageButton);
        //if (this.nextPageButton.getSlot() < getSize()) {
        //}
        return buttons;
    }

    @Override
    public Inventory getInventory() {
        return getPages().get(0).getInventory();
    }

}
