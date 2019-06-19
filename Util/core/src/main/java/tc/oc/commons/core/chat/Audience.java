package tc.oc.commons.core.chat;

import net.md_5.bungee.api.chat.BaseComponent;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * Receiver of chat messages, sounds, titles, and other informational media.
 */
public interface Audience {

    void sendMessage(BaseComponent message);

    default void sendMessages(BaseComponent... lines) {
        Stream.of(lines).forEachOrdered(this::sendMessage);
    }

    default void sendMessages(Iterable<? extends BaseComponent> lines) {
        lines.forEach(this::sendMessage);
    }

    default void sendMessages(Stream<? extends BaseComponent> lines) {
        lines.forEachOrdered(this::sendMessage);
    }

    void sendWarning(BaseComponent message, boolean audible);

    void playSound(Sound sound);

    void stopSound(Sound sound);

    void sendHotbarMessage(BaseComponent message);

    void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks);

    void hideTitle();

    @Deprecated
    default void sendMessage(String message) {
        sendMessage(Component.fromLegacyToComponent(message, true));
    }

    @Deprecated
    default void sendWarning(String message, boolean audible) {
        sendWarning(Component.fromLegacyToComponent(message, true), audible);
    }

}
