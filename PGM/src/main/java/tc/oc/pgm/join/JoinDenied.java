package tc.oc.pgm.join;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Iterables;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import static com.google.common.base.Preconditions.checkNotNull;

public class JoinDenied implements JoinResult {

    private final BaseComponent message;
    private final boolean visible;
    private final boolean error;
    private final List<BaseComponent> extra = new ArrayList<>();

    protected JoinDenied(boolean visible, boolean error, BaseComponent message) {
        this.visible = visible;
        this.error = error;
        this.message = checkNotNull(message);
    }

    public static JoinDenied translate(boolean visible, boolean error, String translate, Object... with) {
        return new JoinDenied(visible, error, new TranslatableComponent(translate, with));
    }

    /**
     * User would generally expect to be able to join right now, but they can't due to
     * some exceptional condition.
     *
     * Example: match full
     */
    public static JoinDenied unavailable(String translate, Object... with) {
        return translate(true, false, translate, with);
    }

    /**
     * User cannot join right now, but they will be able to in the near future,
     * so failure message should look like a friendly reminder rather than an error.
     *
     * Example: match finished
     */
    public static JoinDenied friendly(String translate, Object... with) {
        return translate(false, false, translate, with);
    }

    /**
     * Joining is completely off the table, or doesn't make any sense.
     *
     * Example: already joined, no join permissions
     */
    public static JoinDenied error(String translate, Object... with) {
        return translate(false, true, translate, with);
    }

    public JoinDenied also(BaseComponent message) {
        extra.add(message);
        return this;
    }

    public JoinDenied also(Iterable<BaseComponent> messages) {
        Iterables.addAll(extra, messages);
        return this;
    }

    @Override
    public Optional<BaseComponent> message() {
        return Optional.of(message);
    }

    @Override
    public Collection<BaseComponent> extra() {
        return extra;
    }

    @Override
    public boolean isError() {
        return error;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public boolean isAllowed() {
        return false;
    }
}
