package tc.oc.pgm.menu.gui;

import com.google.api.client.util.Lists;
import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingManager;
import me.anxuiz.settings.Toggleable;
import me.anxuiz.settings.bukkit.PlayerSettings;
import me.anxuiz.settings.bukkit.plugin.Permissions;
import me.anxuiz.settings.types.BooleanType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.gui.buttons.empty.EmptyButton;
import tc.oc.commons.bukkit.gui.interfaces.SinglePageInterface;
import tc.oc.commons.bukkit.tokens.TokenUtil;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.commons.bukkit.util.ItemCreator;
import tc.oc.pgm.PGM;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.mutation.MutationMatchModule;
import tc.oc.pgm.mutation.command.MutationCommands;
import tc.oc.pgm.picker.PickerSettings;
import tc.oc.pgm.tokens.gui.MainTokenMenu;
import tc.oc.pgm.tokens.gui.MutationConfirmInterface;

import java.util.*;

public class SettingsInterface extends SinglePageInterface {

    SettingMenuHelper.SettingType settingType;

    public SettingsInterface(Player player, SettingMenuHelper.SettingType settingType) {
        super(player, new ArrayList<>(), 54, settingType.toString() + "Settings");
        this.settingType = settingType;
        update();
    }

    @Override
    public void setButtons() {
        List<Button> buttons = new ArrayList<>();
        List<Setting> settings = SettingMenuHelper.getAllOfType(settingType);
        Collections.sort(settings, (s1, s2) -> s1.getName().compareTo(s2.getName()));

        for(Iterator<Setting> it = settings.iterator(); it.hasNext(); ) {
            Setting setting = it.next();
            if(!(setting.getType() instanceof Toggleable) ||
                    !getPlayer().hasPermission("setting." + setting.getName().toLowerCase() + ".view") ||
                    !getPlayer().hasPermission("setting." + setting.getName().toLowerCase() + ".set")) {
                it.remove();
            }
        }

        settings.forEach(setting -> buttons.add(getSettingButton(setting)));

        setButtons(buttons);
    }

    private Button getSettingButton(Setting setting) {
        ItemCreator itemCreator = new ItemCreator(Material.PAPER)
                .setName(Constants.PREFIX + setting.getName())
                .addLore(Constants.SUBTEXT + setting.getSummary(),
                         setting.getType().print(PlayerSettings.getManager(getPlayer()).getValue(setting)))
                .setHideFlags(ItemCreator.HideFlag.ALL);
        return new Button(itemCreator) {
            @Override
            public void function(Player player) {
                SettingManager manager = PlayerSettings.getManager(player);
                Object newValue = ((Toggleable) setting.getType()).getNextState(manager.getValue(setting));
                manager.setValue(setting, newValue);
                player.sendMessage(setting.getName() + " toggled to " + setting.getType().print(newValue));
            }
        };
    }

    @Override
    public void setDefaultButtons() {
        defaultButtons.clear();
        defaultButtons.add(new Button(new ItemCreator(Material.WOOL)
                .setData(14)
                .setName(ChatColor.GREEN + "Go Back"), 49) {
            @Override
            public void function(Player player) {
                player.openInventory(new MainMenuInterface(player).getInventory());
            }
        });
        for (Integer integer : new Integer[]{
                 0,  1,  2,  3,  4,  5,  6,  7,  8,
                 9,                             17,
                18,                             26,
                27,                             35,
                36,                             44,
                45, 46, 47, 48,     50, 51, 52, 53}) {
            EmptyButton button = new EmptyButton(integer);
            defaultButtons.add(button);
        }
    }

}
