package tc.oc.commons.bukkit.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public interface ComponentRenderContext {

    BaseComponent render(BaseComponent original, CommandSender viewer);

    default BaseComponent[] render(BaseComponent[] originals, CommandSender viewer) {
        if(originals == null) return null;

        BaseComponent[] replacements = originals;
        int index = 0;

        for(BaseComponent original : originals) {
            BaseComponent replacement = render(original, viewer);

            if(replacement != original && replacements == originals) {
                // If an entry was replaced and we are still using the original list,
                // create the replacement list and copy the entries we've already passed.
                replacements = new BaseComponent[originals.length];
                for(BaseComponent passed : originals) {
                    if(passed == original) break;
                    replacements[index++] = passed;
                }
            }

            if(replacements != originals) {
                // If we have already replaced the list, append the current entry
                replacements[index++] = replacement;
            }
        }

        return replacements;
    }

    default List<BaseComponent> render(List<BaseComponent> originals, CommandSender viewer) {
        if(originals == null) return null;

        List<BaseComponent> replacements = originals;

        for(BaseComponent original : originals) {
            BaseComponent replacement = render(original, viewer);

            if(replacement != original && replacements == originals) {
                // If an entry was replaced and we are still using the original list,
                // create the replacement list and copy the entries we've already passed.
                replacements = new ArrayList<>(originals.size());
                for(BaseComponent passed : originals) {
                    if(passed == original) break;
                    replacements.add(passed);
                }
            }

            if(replacements != originals) {
                // If we have already replaced the list, append the current entry
                replacements.add(replacement);
            }
        }

        return replacements;
    }

    default String renderLegacy(BaseComponent original, CommandSender viewer) {
        return render(original, viewer).toLegacyText();
    }

    default void send(BaseComponent original, CommandSender viewer) {
        viewer.sendMessage(render(original, viewer));
    }

    default void send(BaseComponent[] originals, CommandSender viewer) {
        for(BaseComponent component : originals) send(component, viewer);
    }

    default void send(List<? extends BaseComponent> originals, CommandSender viewer) {
        for(BaseComponent component : originals) send(component, viewer);
    }
}
