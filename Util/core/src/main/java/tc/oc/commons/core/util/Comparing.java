package tc.oc.commons.core.util;

public interface Comparing<T extends Comparable<T>> extends Comparable<Comparing<T>> {

    T compareWith();

    @Override
    default int compareTo(Comparing<T> that) {
        return compareWith().compareTo(that.compareWith());
    }
}
