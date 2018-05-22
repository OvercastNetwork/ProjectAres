package tc.oc.commons.bukkit.chat;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.util.IndexedFunction;
import tc.oc.commons.core.util.Numbers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Paginator<T> {
    public static final int DEFAULT_PER_PAGE = 14;

    private int perPage = DEFAULT_PER_PAGE;
    private @Nullable BaseComponent title;
    private IndexedFunction<? super T, ? extends BaseComponent> formatter =
        (t, i) -> new Component(String.valueOf(t));

    public Paginator() {
        this(DEFAULT_PER_PAGE);
    }

    public Paginator(int perPage) {
        checkArgument(perPage > 0);
        this.perPage = perPage;
    }

    public Paginator<T> perPage(int perPage) {
        this.perPage = perPage;
        return this;
    }

    public Paginator<T> title(@Nullable BaseComponent title) {
        this.title = title;
        return this;
    }

    public Paginator<T> entries(IndexedFunction<? super T, ? extends BaseComponent> formatter) {
        this.formatter = checkNotNull(formatter);
        return this;
    }

    public void display(CommandSender sender, Collection<? extends T> results, int page) {
        display(Audiences.Deprecated.get(sender), results, page);
    }

    public void display(Audience audience, Collection<? extends T> results, int page) {
        if(results.isEmpty()) {
            audience.sendMessage(new WarningComponent("command.error.emptyResult"));
            return;
        }

        final int pages = Numbers.divideRoundingUp(results.size(), perPage);

        if(page < 1 || page > pages) {
            audience.sendMessage(new WarningComponent("command.error.invalidPage", String.valueOf(page), String.valueOf(pages)));
            return;
        }

        final int start = perPage * (page - 1);
        final int end = Math.min(perPage * page, results.size());

        audience.sendMessage(header(page, pages));

        if(results instanceof List) {
            List<? extends T> list = (List<? extends T>) results;
            for (int index = start; index < end; index++) {
                audience.sendMessages(multiEntry(list.get(index), index));
            }
        } else {
            final Iterator<? extends T> iterator = results.iterator();
            for(int index = Iterators.advance(iterator, start); index < end; index++) {
                audience.sendMessages(multiEntry(iterator.next(), index));
            }
        }
    }

    public BaseComponent header(int page, int pages) {
        final Component c = new Component(ChatColor.GRAY);
        final BaseComponent title = title();
        if(title != null) {
            c.extra(title, ChatColor.BLUE);
        }
        c.extra(" (")
         .extra(new TranslatableComponent("pageHeader",
                                          new Component(String.valueOf(page), ChatColor.WHITE),
                                          new Component(String.valueOf(pages), ChatColor.WHITE)
         ))
         .extra(")");
        return new HeaderComponent(c);
    }

    protected @Nullable BaseComponent title() {
        return title;
    }

    protected BaseComponent entry(T entry, int index) {
        return formatter.apply(entry, index);
    }

    protected List<? extends BaseComponent> multiEntry(T entry, int index) {
        return ImmutableList.of(entry(entry, index));
    }
}
