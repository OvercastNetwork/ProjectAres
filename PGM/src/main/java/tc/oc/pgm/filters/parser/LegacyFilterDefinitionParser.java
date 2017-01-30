package tc.oc.pgm.filters.parser;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import org.jdom2.Element;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.block.MaterialFilter;
import tc.oc.pgm.filters.operator.AnyFilter;
import tc.oc.pgm.filters.operator.FilterNode;
import tc.oc.pgm.filters.operator.InverseFilter;
import tc.oc.pgm.utils.MaterialPattern;
import tc.oc.pgm.utils.MethodParser;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowFunction;

public class LegacyFilterDefinitionParser extends FilterDefinitionParser {

    protected List<? extends Filter> parseGrandchildren(Element parent, String childName) throws InvalidXMLException {
        return parent.getChildren(childName)
                     .stream()
                     .flatMap(rethrowFunction(filterParser::parseChildren))
                     .collect(Collectors.toList());
    }

    protected List<? extends Filter> parseParents(Element el) throws InvalidXMLException {
        return Node.tryAttr(el, "parents")
                   .map(attr -> Stream.of(attr.getValueNormalize().split("\\s"))
                                      .map(rethrowFunction(name -> filterParser.parseReference(attr, name)))
                                      .collect(tc.oc.commons.core.stream.Collectors.toImmutableList()))
                   .orElse(ImmutableList.of());
    }

    // Deprecated uses of <filter>
    @MethodParser("filter")
    public Filter parseFilter(Element el) throws InvalidXMLException {
        if(el.getAttribute("parents") != null || el.getChild("allow") != null || el.getChild("deny") != null) {
            // A weird node thing
            return new FilterNode(parseParents(el),
                                  parseGrandchildren(el, "allow"),
                                  parseGrandchildren(el, "deny"));
        } else {
            // An alias for <all> (is this actually used anywhere?)
            return parseAll(el);
        }
    }

    // Deprecated alias for <material> removed to avoid conflict with <block> region
    @MethodParser("block")
    public Filter parseBlock(Element el) throws InvalidXMLException {
        MaterialPattern pattern = XMLUtils.parseMaterialPattern(el);
        if(!pattern.getMaterial().isBlock()) {
            throw new InvalidXMLException("Material is not a block", el);
        }
        return new MaterialFilter(pattern);
    }

    // Deprecated <not> syntax with multiple child filters
    @MethodParser("not")
    public Filter parseNot(Element el) throws InvalidXMLException {
        return new InverseFilter(new AnyFilter(filterParser.parseChildList(el)));
    }
}
