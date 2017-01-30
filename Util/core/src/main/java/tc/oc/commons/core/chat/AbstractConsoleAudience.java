package tc.oc.commons.core.chat;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;

public abstract class AbstractConsoleAudience extends AbstractAudience {

    @Override
    public void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
        if(title != null) sendMessage(new Component("[Title] ").extra(title));
        if(subtitle != null) sendMessage(new Component("[Subtitle] ").extra(subtitle));
    }

    @Override
    public void hideTitle() {}

    @Override
    public void sendHotbarMessage(BaseComponent message) {
        sendMessage(new Component("[Hotbar] ").extra(message));
    }

    @Override
    public void playSound(Sound sound) {}
}
