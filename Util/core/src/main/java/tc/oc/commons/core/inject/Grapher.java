package tc.oc.commons.core.inject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.common.base.Charsets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.grapher.graphviz.GraphvizGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;

/**
 * https://github.com/google/guice/wiki/Grapher
 */
public class Grapher {
    public void writeGraph(File file, Injector injector) throws IOException {
        final PrintWriter out = new PrintWriter(file, Charsets.UTF_8.name());
        final GraphvizGrapher grapher = Guice.createInjector(new GraphvizModule()).getInstance(GraphvizGrapher.class);

        grapher.setOut(out);
        grapher.setRankdir("TB");
        grapher.graph(injector);
    }
}
