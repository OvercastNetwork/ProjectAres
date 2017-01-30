package tc.oc.commons.bukkit.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.localization.Translator;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TranslatableComponentRenderer extends BaseComponentRenderer<TranslatableComponent> {

    private final Translator translations;

    @Inject TranslatableComponentRenderer(Translator translations) {
        this.translations = translations;
    }

    @Override
    public BaseComponent renderContent(ComponentRenderContext context, TranslatableComponent original, CommandSender viewer) {
        final List<BaseComponent> with = context.render(original.getWith(), viewer);
        final Optional<MessageFormat> pattern = translations.pattern(original.getTranslate(), viewer);

        if(pattern.isPresent()) {
            // Found a TranslatableComponent with one of our keys
            return new Component(Components.format(pattern.get(), with));
        } else if(with != original.getWith()) {
            // Not our key, but something in with was replaced
            final TranslatableComponent replacement = new TranslatableComponent(original.getTranslate());
            replacement.setWith(with);
            return replacement;
        } else {
            // Nothing was replaced
            return original;
        }
    }
}
