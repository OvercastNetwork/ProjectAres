package tc.oc.commons.bukkit.localization;

import com.google.inject.TypeLiteral;
import java.util.Map;
import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.core.inject.Manifest;
import tc.oc.commons.core.reflect.TypeLiterals;
import tc.oc.parse.ParserTypeLiterals;

public class LocalizationManifest extends Manifest implements TypeLiterals, ParserTypeLiterals {

    @Override
    protected void configure() {
        installFactory(LocalizedMessageMap.Factory.class);
        installFactory(new TypeLiteral<LocalizedDocument.Factory<Map<String, BaseComponent>>>(){});

        bind(DocumentParser(Map(String.class, BaseComponent.class)))
            .to(MessageMapParser.class);
    }
}
