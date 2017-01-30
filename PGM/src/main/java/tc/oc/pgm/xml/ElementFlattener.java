package tc.oc.pgm.xml;

import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import org.jdom2.Element;

/**
 * Builds a list of descendant {@link Element}s from a parent element,
 * and inherits attributes down the tree.
 */
public class ElementFlattener {

    public interface Filter {
        boolean test(Element element, int depth);
    }

    private final Filter branchFilter;
    private final Filter leafFilter;

    public ElementFlattener(Filter branchFilter, Filter leafFilter) {
        this.branchFilter = branchFilter;
        this.leafFilter = leafFilter;
    }

    public ElementFlattener(Set<String> parentTagNames, @Nullable Set<String> childTagNames, int minChildDepth) {
        this((el, depth) -> parentTagNames.contains(el.getName()),
             (el, depth) -> depth >= minChildDepth && (childTagNames == null || childTagNames.contains(el.getName())));
    }

    public Stream<Element> flattenChildren(Element parent) {
        return parent.getChildren()
                     .stream()
                     .flatMap(this::flatten);
    }

    public Stream<Element> flatten(Element element) {
        return flatten(element, 0);
    }

    private Stream<Element> flatten(Element element, int depth) {
        if(branchFilter.test(element, depth)) {
            return wrap(element, depth).getChildren()
                                       .stream()
                                       .flatMap(child -> flatten(child, depth + 1));
        } else if(leafFilter.test(element, depth)) {
            return Stream.of(wrap(element, depth));
        } else {
            return Stream.empty();
        }
    }

    private Element wrap(Element element, int depth) {
        // Root node has nothing to inherit
        return depth < 1 ? element : InheritingElement.of(element);
    }
}
