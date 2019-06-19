package tc.oc.pgm.menu.gui;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.bukkit.PlayerSettings;
import tc.oc.commons.bukkit.broadcast.BroadcastSettings;
import tc.oc.commons.bukkit.punishment.PunishmentMessageSetting;
import tc.oc.commons.bukkit.users.JoinMessageSetting;
import tc.oc.commons.bukkit.whisper.WhisperSettings;
import tc.oc.pgm.damage.DamageSettings;
import tc.oc.pgm.death.DeathMessageSetting;
import tc.oc.pgm.picker.PickerSettings;
import tc.oc.pgm.playerstats.StatSettings;
import tc.oc.pgm.settings.ObserverSetting;
import tc.oc.pgm.settings.Settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SettingMenuHelper {

    private static Map<Setting, SettingType> settings = new HashMap<>();

    public static void initializeSettings() {
        Setting[] chat = {DeathMessageSetting.get(), JoinMessageSetting.get(), WhisperSettings.receive(), PunishmentMessageSetting.get(), BroadcastSettings.TIPS};
        Setting[] gameplay = {DamageSettings.ATTACK_SPEEDOMETER, DamageSettings.DAMAGE_NUMBERS, DamageSettings.KNOCKBACK_PARTICLES, Settings.BLOOD};
        Setting[] misc = {ObserverSetting.get(), PickerSettings.PICKER, Settings.SOUNDS, WhisperSettings.sound(), StatSettings.STATS};

        for (Setting s : chat) { settings.put(s,SettingType.CHAT); }
        for (Setting s : gameplay) { settings.put(s,SettingType.GAMEPLAY); }
        for (Setting s : misc) { settings.put(s,SettingType.MISC); }
    }

    public static List<Setting> getAllOfType(SettingType settingType) {
        return PlayerSettings.getRegistry().getSettings().stream().filter(setting -> getSettingType(setting) == settingType).collect(Collectors.toList());
    }

    private static SettingType getSettingType(Setting setting) {
        return settings.getOrDefault(setting, SettingType.MISC);
    }

    public enum SettingType {
        CHAT,GAMEPLAY,MISC
    }
}
