package tc.oc.commons.core.chat;

import java.util.stream.Stream;
import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Receiver of chat messages, sounds, titles, and other informational media.
 * Can represent any number of actual recipients.
 */
public interface Audience {

    /** Send a message to chat */
    void sendMessage(BaseComponent message);

    default void sendMessages(BaseComponent... lines) {
        for(BaseComponent line : lines) {
            sendMessage(line);
        }
    }

    default void sendMessages(Iterable<? extends BaseComponent> lines) {
        for(BaseComponent line : lines) {
            sendMessage(line);
        }
    }

    default void sendMessages(Stream<? extends BaseComponent> lines) {
        lines.forEach(this::sendMessage);
    }

    /** Send a message to chat styled as a warning, with an optional audio cue */
    void sendWarning(BaseComponent message, boolean audible);

    /** Play a sound (by raw asset name) */
    void playSound(Sound sound);

    /** Send a message to the display slot above the hotbar */
    void sendHotbarMessage(BaseComponent message);

    /** Show a title and/or subtitle  */
    void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks);

    /** Hide any titles that are currently showing */
    void hideTitle();

    /** Use {@link #sendMessage(BaseComponent) */
    @Deprecated
    void sendMessage(String message);

    /** Use {@link #sendWarning(BaseComponent, boolean) */
    @Deprecated
    void sendWarning(String message, boolean audible);
}
