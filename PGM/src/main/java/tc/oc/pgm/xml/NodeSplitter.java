package tc.oc.pgm.xml;

import java.util.stream.Stream;
import javax.inject.Singleton;

public interface NodeSplitter {
    Stream<String> split(Node node);

    /**
     * The node name as a single value
     */
    @Singleton
    class Name implements NodeSplitter {
        @Override public Stream<String> split(Node node) {
            return Stream.of(node.getName());
        }
    }

    /**
     * The node content as a single value
     */
    @Singleton
    class Atom implements NodeSplitter {
        @Override public Stream<String> split(Node node) {
            return Stream.of(node.getValue());
        }
    }

    /**
     * The node content, split by spaces/commas
     */
    @Singleton
    class List implements NodeSplitter {
        @Override public Stream<String> split(Node node) {
            return Stream.of(node.getValue().split("\\s+|\\s*,\\s*"));
        }
    }

    NodeSplitter NAME = new Name();
    NodeSplitter ATOM = new Atom();
    NodeSplitter LIST = new List();
}
