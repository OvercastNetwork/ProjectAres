package tc.oc.commons.bukkit.markup;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import tc.oc.commons.bukkit.chat.LinkComponent;
import tc.oc.commons.bukkit.chat.Links;
import tc.oc.commons.bukkit.chat.UserURI;
import tc.oc.commons.bukkit.chat.Renderable;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.parse.MissingException;
import tc.oc.parse.ParseException;
import tc.oc.parse.ValueException;
import tc.oc.parse.xml.XML;
import tc.oc.parse.xml.ElementParser;
import tc.oc.parse.xml.NodeParser;
import tc.oc.parse.xml.UnrecognizedNodeException;

/**
 * Parses an XML dialect similar to HTML into {@link BaseComponent}s
 *
 * The following elements are supported:
 *
 *     <i>italic</i>
 *     <b>bold</b>
 *     <em>emphasis (gold colored)</em>
 *     <a href="...">link</a>
 *
 * The following attributes work on ANY element:
 *
 *     color="..."
 *     bold="true/false"
 *     italic="true/false"
 *     underline="true/false"
 *
 * The anchor element has an extra attribute "type" that changes how
 * the "href" attribute is interpreted:
 *
 *     url      Standard URL (default)
 *     home     Path relative to network website
 *     user     Path relative to user profile on website
 *
 * An anchor element with no content will display a compact form of the URL.
 *
 * The {@link #parse(Node)} and {@link #parse(Element)} methods expect a recognized node,
 * whereas {@link #content(Element, List, ChatColor...)} will accept any {@link Element},
 * parsing its attributes and content.
 */
public class MarkupParser implements NodeParser<BaseComponent>, ElementParser<BaseComponent> {

    private final NodeParser<ChatColor> colorParser;
    private final NodeParser<Boolean> booleanParser;

    @Inject private MarkupParser(NodeParser<ChatColor> colorParser, NodeParser<Boolean> booleanParser) {
        this.colorParser = colorParser;
        this.booleanParser = booleanParser;
    }

    @Override
    public BaseComponent parse(Node node) {
        switch(node.getNodeType()) {
            case Node.TEXT_NODE:
                // We use {blank} to force Crowdin to include things in strings that it would otherwise exclude,
                // such as empty tags at the beginning/end of the string e.g.
                //
                //     Click here <a type="home"/>{blank}
                //
                // The {blank} forces the <a/> to be part of the string, and allows translators to move it around.
                final String text = node.getNodeValue();
                return "{blank}".equals(text) ? Components.blank()
                                              : new Component(node.getNodeValue());
            case Node.ELEMENT_NODE:
                return parse((Element) node);
            default:
                return Components.blank();
        }
    }

    @Override
    public BaseComponent parse(Element el) throws ParseException {
        // Crowdin only allows a few hard-coded tag names to be part of strings,
        // so those are the only names we can use here. Any other tags will
        // split the text into multiple strings. We use custom attributes to
        // overload the meaning of these few tags.
        switch(el.getTagName()) {
            case "i": return content(el, ChatColor.ITALIC);
            case "b": return content(el, ChatColor.BOLD);
            case "em": return content(el, ChatColor.GOLD);
            case "a": return anchor(el);
        }
        throw new UnrecognizedNodeException(el);
    }

    BaseComponent anchor(Element el) {
        final Optional<String> href = XML.attrValue(el, "href");
        final Optional<BaseComponent> content = nonEmptyContent(el);
        final String type = XML.attrValue(el, "type").orElse("url");
        final Renderable<URI> uri;

        try {
            switch(type) {
                case "user":
                    uri = new UserURI(href.orElse(""));
                    break;

                case "home":
                    uri = Renderable.of(Links.homeUri(href.orElse("/")));
                    break;

                case "url":
                    uri = Renderable.of(new URI(href.orElseThrow(() -> new MissingException("attribute", "href"))));
                    break;

                default:
                    throw new ValueException("Unknown anchor type '" + type + "'");
            }
        } catch(URISyntaxException e) {
            throw new ValueException(e.getMessage());
        }

        return new LinkComponent(uri, content);
    }

    public BaseComponent content(Element el, ChatColor... formats) {
        return content(el, children(el).collect(Collectors.toList()), formats);
    }

    public Optional<BaseComponent> nonEmptyContent(Element el, ChatColor... formats) {
        final List<BaseComponent> children = children(el).collect(Collectors.toList());
        return children.isEmpty() ? Optional.empty()
                                  : Optional.of(content(el, children, formats));
    }

    private BaseComponent content(Element el, List<BaseComponent> children, ChatColor... formats) {
        final Component c = new Component(children, formats);
        XML.attr(el, "bold").map(booleanParser::parse).ifPresent(c::bold);
        XML.attr(el, "italic").map(booleanParser::parse).ifPresent(c::italic);
        XML.attr(el, "underline").map(booleanParser::parse).ifPresent(c::underlined);
        XML.attr(el, "color").map(colorParser::parse).ifPresent(c::add);
        return c;
    }

    private Stream<BaseComponent> children(Element el) {
        return XML.childNodes(el)
                  .map(this::parse)
                  .filter(node -> !Components.blank().equals(node));
    }
}
