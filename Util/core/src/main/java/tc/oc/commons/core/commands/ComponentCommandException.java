package tc.oc.commons.core.commands;

import com.sk89q.minecraft.util.commands.CommandException;
import net.md_5.bungee.api.chat.BaseComponent;

public class ComponentCommandException extends CommandException {

    private final BaseComponent message;

    public ComponentCommandException(BaseComponent message) {
        super(message.toLegacyText());
        this.message = message;
    }

    public BaseComponent getComponentMessage() {
        return message;
    }
}
