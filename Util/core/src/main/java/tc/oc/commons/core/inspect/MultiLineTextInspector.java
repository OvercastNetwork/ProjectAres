package tc.oc.commons.core.inspect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.base.Strings;
import tc.oc.commons.core.util.Chain;
import tc.oc.commons.core.util.Streams;

public class MultiLineTextInspector implements Inspector<Stream<String>> {

    private final TextInspector text = new TextInspector();

    private Stream<String> indent(Stream<String> lines) {
        return lines.map(line -> "  " + line);
    }

    private Stream<String> enclose(String open, Stream<String> lines, String close) {
        final List<String> list = lines.collect(Collectors.toList());
        return list.isEmpty() ? Stream.of(open + close)
                              : Streams.concat(Stream.of(open),
                                               indent(list.stream()),
                                               Stream.of(close));
    }

    private Stream<String> flatten(String delimiter, Stream<Stream<String>> bumpy) {
        final List<String> flat = new ArrayList<>();
        bumpy.forEachOrdered(lines -> {
            if(!flat.isEmpty()) {
                final int last = flat.size() - 1;
                flat.set(last, flat.get(last) + delimiter);
            }
            lines.forEachOrdered(flat::add);
        });
        return flat.stream();
    }

    @Override
    public Stream<String> scalar(@Nullable Object value, Inspection options) {
        return Stream.of(text.scalar(value, options));
    }

    @Override
    public <E> Stream<String> collection(Collection<E> collection, Stream<Stream<String>> elements, Inspection options) {
        return enclose("[", indent(flatten(",", elements)), "]");
    }

    private int width(Stream<String> lines) {
        return lines.mapToInt(String::length).max().orElse(0);
    }

    private Stream<String> association(Stream<String> left, String middle, Stream<String> right) {
        final List<String> leftList = left.collect(Collectors.toList());
        final List<String> rightList = right.collect(Collectors.toList());

        final int height = Math.max(leftList.size(), rightList.size());
        final int leftWidth = width(leftList.stream());

        final String leftPad = leftList.size() < 2 ? "" : Strings.repeat(" ", leftWidth);
        final String middlePad = leftList.size() < 2 ? "" : Strings.repeat(" ", middle.length());

        final List<String> result = new ArrayList<>();
        for(int i = 0; i < height; i++) {
            result.add((i < leftList.size() ? Strings.padEnd(leftList.get(i), leftWidth, ' ') : leftPad) +
                       (i == 0 ? middle : middlePad) +
                       (i < rightList.size() ? rightList.get(i) : ""));
        }
        return result.stream();
    }

    @Override
    public <K, V> Stream<String> map(Map<K, V> map, Stream<Map.Entry<Stream<String>, Stream<String>>> entries, Inspection options) {
        return enclose("{", indent(flatten(",", entries.map(entry -> association(entry.getKey(), " -> ", entry.getValue())))), "}");
    }

    @Override
    public Stream<String> reference(Inspectable inspectable, Inspection options) {
        return Stream.of(text.reference(inspectable, options));
    }

    @Override
    public Stream<String> inspectable(Inspectable inspectable, Stream<Map.Entry<String, Stream<String>>> properties, Inspection options) {
        return enclose(inspectable.identify() + "{",
                       indent(flatten(",", properties.map(property -> association(Stream.of(property.getKey()), " = ", property.getValue())))),
                       "}");
    }

    @Override
    public Stream<String> exception(InspectionException e, Inspection options) {
        return Stream.of(text.exception(e, options));
    }

    @Override
    public Stream<String> cycle(Object value, Chain<Object> path, Inspection options) {
        return Stream.of(text.cycle(value, path, options));
    }
}
