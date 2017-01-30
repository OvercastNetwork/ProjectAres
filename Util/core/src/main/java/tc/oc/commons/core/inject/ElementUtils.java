package tc.oc.commons.core.inject;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateBinder;
import com.google.inject.Stage;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.Elements;

public class ElementUtils {
    private ElementUtils() {}

    public static List<Element> visit(ElementVisitor<?> visitor, Iterable<? extends Module> modules) {
        final List<Element> elements = Elements.getElements(Stage.TOOL, modules);
        elements.forEach(e -> e.acceptVisitor(visitor));
        return elements;
    }

    public static List<Element> visit(ElementVisitor<?> visitor, Module... modules) {
        return visit(visitor, Arrays.asList(modules));
    }

    public static void print(PrintStream stream, Iterable<? extends Module> modules) {
        visit(new ElementPrinter(stream), modules);
    }

    public static void print(PrintStream stream, Module... modules) {
        visit(new ElementPrinter(stream), modules);
    }

    public static void print(Iterable<? extends Module> modules) {
        print(System.out, modules);
    }

    public static void print(Module... modules) {
        print(System.out, modules);
    }

    public static void log(Module... modules) {
        log(Logger.getGlobal(), modules);
    }

    public static void log(Logger logger, Module... modules) {
        log(logger, Level.INFO, modules);
    }

    public static void log(Logger logger, Level level, Module... modules) {
        visit(new ElementLogger(logger, level), modules);
    }

    public static void log(Logger logger, Level level, Iterable<? extends Module> modules) {
        visit(new ElementLogger(logger, level), modules);
    }

    public static void expose(PrivateBinder binder, Iterable<? extends Module> modules) {
        visit(new ElementExposer(binder), modules);
    }

    public static void expose(PrivateBinder binder, Module... modules) {
        visit(new ElementExposer(binder), modules);
    }

    public static <T> Optional<Binding<T>> findBinding(Iterable<? extends Element> elements, Key<T> key) {
        for(Element element : elements) {
            if(element instanceof Binding && key.equals(((Binding<?>) element).getKey())) {
                return Optional.of((Binding<T>) element);
            }
        }
        return Optional.empty();
    }
}
