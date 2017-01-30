package tc.oc.minecraft.server;

import javax.inject.Inject;

import org.w3c.dom.Element;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.parse.ParseException;
import tc.oc.parse.xml.ElementParser;
import tc.oc.parse.xml.NodeParser;
import tc.oc.parse.xml.XML;

/**
 * Parses a {@link ServerFilter} from any {@link Element} by collecting
 * child elements named after the filter fields. Any element can appear
 * multiple times.
 *
 *     <whatever>
 *         <role>...</role>             {@link ServerFilter#roles()}
 *         <network>...</network>       {@link ServerFilter#networks()}
 *         <realm>...</realm>           {@link ServerFilter#realms()}
 *         <game>...</game>             {@link ServerFilter#games()}
 *         <gamemode>...</gamemode>     {@link ServerFilter#gamemodes()}
 *     </whatever>
 */
public class ServerFilterParser implements ElementParser<ServerFilter> {

    private final NodeParser<ServerDoc.Role> roleParser;
    private final NodeParser<ServerDoc.Network> networkParser;
    private final NodeParser<MapDoc.Gamemode> gamemodeParser;

    @Inject ServerFilterParser(NodeParser<ServerDoc.Role> roleParser, NodeParser<ServerDoc.Network> networkParser, NodeParser<MapDoc.Gamemode> gamemodeParser) {
        this.roleParser = roleParser;
        this.networkParser = networkParser;
        this.gamemodeParser = gamemodeParser;
    }

    @Override
    public ServerFilter parse(Element el) throws ParseException {
        return new ServerFilter(
            XML.childrenNamed(el, "role").map(roleParser::parse),
            XML.childrenNamed(el, "network").map(networkParser::parse),
            XML.childrenNamed(el, "realm").map(Element::getTextContent),
            XML.childrenNamed(el, "game").map(Element::getTextContent),
            XML.childrenNamed(el, "gamemode").map(gamemodeParser::parse)
        );
    }
}
