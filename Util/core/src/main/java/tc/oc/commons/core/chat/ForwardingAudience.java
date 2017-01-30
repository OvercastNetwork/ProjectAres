package tc.oc.commons.core.chat;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;

public interface ForwardingAudience extends Audience {

    Audience audience();

    @Override
    default void sendMessage(BaseComponent message) {
        audience().sendMessage(message);
    }

    @Override
    default void sendWarning(BaseComponent message, boolean audible) {
        audience().sendWarning(message, audible);
    }

    @Override
    default void playSound(Sound sound) {
        audience().playSound(sound);
    }

    @Override
    default void sendHotbarMessage(BaseComponent message) {
        audience().sendHotbarMessage(message);
    }

    @Override
    default void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
        audience().showTitle(title, subtitle, inTicks, stayTicks, outTicks);
    }

    @Override
    default void hideTitle() {
        audience().hideTitle();
    }

    @Override
    default void sendMessage(String message) {
        audience().sendMessage(message);
    }

    @Override
    default void sendWarning(String message, boolean audible) {
        audience().sendWarning(message, audible);
    }
}
