package tc.oc.commons.core.chat;

import net.md_5.bungee.api.chat.BaseComponent;

import javax.annotation.Nullable;
import java.util.Optional;

@FunctionalInterface
public interface ForwardingAudience extends Audience {

    Optional<Audience> audience();

    @Override
    default void sendMessage(BaseComponent message) {
        audience().ifPresent(audience -> audience.sendMessage(message));
    }

    @Override
    default void sendWarning(BaseComponent message, boolean audible) {
        audience().ifPresent(audience -> audience.sendWarning(message, audible));
    }

    @Override
    default void playSound(Sound sound) {
        audience().ifPresent(audience -> audience.playSound(sound));
    }

    @Override
    default void stopSound(Sound sound) {
        audience().ifPresent(audience -> audience.stopSound(sound));
    }

    @Override
    default void sendHotbarMessage(BaseComponent message) {
        audience().ifPresent(audience -> audience.sendHotbarMessage(message));
    }

    @Override
    default void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
        audience().ifPresent(audience -> audience.showTitle(title, subtitle, inTicks, stayTicks, outTicks));
    }

    @Override
    default void hideTitle() {
        audience().ifPresent(audience -> audience.hideTitle());
    }

    @Override
    default void sendMessage(String message) {
        audience().ifPresent(audience -> audience.sendMessage(message));
    }

    @Override
    default void sendWarning(String message, boolean audible) {
        audience().ifPresent(audience -> audience.sendWarning(message, audible));
    }
}
