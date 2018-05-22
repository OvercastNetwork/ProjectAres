package tc.oc.commons.core.chat;

import net.md_5.bungee.api.chat.BaseComponent;

import javax.annotation.Nullable;
import java.util.Optional;

public interface ConsoleAudience extends Audience {

    @Override
    default void playSound(Sound sound) {}

    @Override
    default void stopSound(Sound sound) {}

    @Override
    default void hideTitle() {}

    @Override
    default void sendHotbarMessage(BaseComponent message) {
        sendMessage(new Component("[Hotbar] ").extra(message));
    }

    @Override
    default void sendWarning(BaseComponent component, boolean audible) {
        sendMessage(new Component("[Warning] ").extra(component));
    }

    @Override
    default void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
        Optional.ofNullable(title).ifPresent(t -> sendMessage(new Component("[Title] ").extra(t)));
        Optional.ofNullable(subtitle).ifPresent(s -> sendMessage(new Component("[Subtitle] ").extra(s)));
    }

}
