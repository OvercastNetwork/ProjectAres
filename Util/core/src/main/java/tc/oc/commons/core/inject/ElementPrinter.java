package tc.oc.commons.core.inject;

import java.io.PrintStream;

import com.google.inject.spi.Message;

public class ElementPrinter extends ElementInspector<Void> {

    private final PrintStream out;

    public ElementPrinter(PrintStream out) {
        this.out = out;
    }

    @Override
    public Void visit(Message message) {
        out.println(message.getMessage() + " (at " + message.getSource() + ")");
        return null;
    }
}
