package tc.oc.pgm.teams;

import java.util.Optional;

import org.jdom2.Element;
import tc.oc.pgm.features.LegacyFeatureParser;
import tc.oc.pgm.xml.InvalidXMLException;

class TeamParser extends LegacyFeatureParser<TeamFactory> {
    /**
     * Some legacy maps assume "X" and "X Team" are equivalent,
     * so remove the " Team" part when normalizing.
     */
    @Override
    public String mangleId(String unmangled) {
        if(legacy) {
            return super.mangleId(unmangled).replaceAll("-team$", "");
        }
        return super.mangleId(unmangled);
    }

    /**
     * Get ID from team name for legacy XML
     */
    @Override
    public Optional<String> parseDefinitionId(Element el, TeamFactory definition) throws InvalidXMLException {
        if(legacy) {
            final String id = el.getAttributeValue("id");
            if(id != null) return Optional.of(id);
            return Optional.of(definition.getDefaultName());
        }
        return super.parseDefinitionId(el, definition);
    }
}
