package tc.oc.commons.core.chat;

import net.md_5.bungee.api.chat.BaseComponent;

import javax.annotation.Nullable;
import java.util.stream.Stream;

@FunctionalInterface
public interface MultiAudience extends Audience {

    Stream<? extends Audience> audiences();

    @Override
    default void sendMessage(BaseComponent message) {
        audiences().forEach(audience -> audience.sendMessage(message));
    }

    @Override
    default void sendWarning(BaseComponent message, boolean audible) {
        audiences().forEach(audience -> audience.sendWarning(message, audible));
    }

    @Override
    default void playSound(Sound sound) {
        audiences().forEach(audience -> audience.playSound(sound));
    }

    @Override
    default void stopSound(Sound sound) {
        audiences().forEach(audience -> audience.stopSound(sound));
    }

    @Override
    default void sendHotbarMessage(BaseComponent message) {
        audiences().forEach(audience -> audience.sendHotbarMessage(message));
    }

    @Override
    default void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
        audiences().forEach(audience -> audience.showTitle(title, subtitle, inTicks, stayTicks, outTicks));
    }

    @Override
    default void hideTitle() {
        audiences().forEach(audience -> audience.hideTitle());
    }

    @Override
    default void sendMessage(String message) {
        audiences().forEach(audience -> audience.sendMessage(message));
    }

    @Override
    default void sendWarning(String message, boolean audible) {
        audiences().forEach(audience -> audience.sendWarning(message, audible));
    }

}
