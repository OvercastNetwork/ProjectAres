package tc.oc.commons.core.inspect;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import tc.oc.commons.core.util.Chain;

public interface Inspector<R> {

    R scalar(@Nullable Object value, Inspection options);

    <E> R collection(Collection<E> collection, Stream<R> elements, Inspection options);

    <K, V> R map(Map<K, V> map, Stream<Map.Entry<R, R>> entries, Inspection options);

    R reference(Inspectable inspectable, Inspection options);

    R inspectable(Inspectable inspectable, Stream<Map.Entry<String, R>> properties, Inspection options);

    R exception(InspectionException e, Inspection options);

    R cycle(Object value, Chain<Object> path, Inspection options);
}
