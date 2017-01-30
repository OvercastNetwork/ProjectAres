package tc.oc.commons.core.commands;

import net.md_5.bungee.api.chat.TranslatableComponent;

public class TranslatableCommandException extends ComponentCommandException {
    public TranslatableCommandException(String translate, Object... with) {
        super(new TranslatableComponent(translate, with));
    }
}
