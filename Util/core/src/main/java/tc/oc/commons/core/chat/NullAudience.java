package tc.oc.commons.core.chat;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;

public class NullAudience implements Audience {

    private NullAudience() {}
    public static final NullAudience INSTANCE = new NullAudience();

    @Override
    public void sendMessage(BaseComponent message) {
    }

    @Override
    public void sendWarning(BaseComponent message, boolean audible) {
    }

    @Override
    public void playSound(Sound sound) {
    }

    @Override
    public void sendHotbarMessage(BaseComponent message) {
    }

    @Override
    public void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
    }

    @Override
    public void hideTitle() {
    }

    @Override
    public void sendMessage(String message) {
    }

    @Override
    public void sendWarning(String message, boolean audible) {
    }
}
