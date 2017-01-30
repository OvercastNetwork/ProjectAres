package tc.oc.pgm.match;

import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.core.chat.ChatUtils;

public abstract class ObservingParty extends MultiPlayerParty {

    private String coloredName;
    private BaseComponent componentName;
    private BaseComponent chatPrefix;

    public ObservingParty(Match match) {
        super(match);
    }

    @Override
    public String getName() {
        return getDefaultName();
    }

    @Override
    public String getName(@Nullable CommandSender viewer) {
        return getName();
    }

    @Override
    public boolean isNamePlural() {
        return true;
    }

    public ChatColor getBungeeColor() {
        return ChatUtils.convert(getColor());
    }

    @Override
    public Color getFullColor() {
        return BukkitUtils.colorOf(this.getColor());
    }

    @Override
    public String getColoredName() {
        if(coloredName == null) {
            coloredName = getColor() + getName();
        }
        return coloredName;
    }

    @Override
    public String getColoredName(@Nullable CommandSender viewer) {
        return getColoredName();
    }

    @Override
    public BaseComponent getComponentName() {
        if(componentName == null) {
            componentName = new Component(getName(), getBungeeColor());
        }
        return componentName;
    }

    @Override
    public BaseComponent getStyledName(NameStyle style) {
        return getComponentName();
    }

    @Override
    public BaseComponent getChatPrefix() {
        if(chatPrefix == null) {
            chatPrefix = new Component("[" + getName() + "] ", getBungeeColor());
        }
        return chatPrefix;
    }

    @Override
    public boolean isAutomatic() {
        return false;
    }

    @Override
    public Type getType() {
        return Type.Observing;
    }

    @Override
    public boolean isParticipatingType() {
        return false;
    }

    @Override
    public boolean isParticipating() {
        return false;
    }

    @Override
    public boolean isObservingType() {
        return true;
    }

    @Override
    public boolean isObserving() {
        return true;
    }
}
