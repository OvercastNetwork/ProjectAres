package tc.oc.pgm.xml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import org.jdom2.Element;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.pgm.xml.finder.NodeFinder;
import tc.oc.pgm.xml.parser.PrimitiveParser;
import tc.oc.pgm.xml.validate.Validation;

/**
 * A reflectively parsed object.
 *
 * Instances are generated automatically from XML, based on the names
 * and types of the interface methods.
 */
public interface Parseable extends Inspectable {
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
    @interface Property {
        String name() default "";
        String[] alias() default {};
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
    @interface Nodes {
        Class<? extends NodeFinder>[] value();
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
    @interface Split {
        Class<? extends NodeSplitter> value();
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
    @interface Parse {
        Class<? extends PrimitiveParser> value();
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
    @interface Validate {
        Class<? extends Validation>[] value();
    }

    /**
     * Indicates that a property is obsolete, and should not appear in documentation
     */
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
    @interface Legacy {}

    default Optional<Element> sourceElement() {
        throw new UnsupportedOperationException();
    }

    default Map<Method, Object> parsedValues() {
        throw new UnsupportedOperationException();
    }
}
