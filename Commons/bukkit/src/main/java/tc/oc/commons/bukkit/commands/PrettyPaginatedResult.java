package tc.oc.commons.bukkit.commands;

import com.google.common.base.Preconditions;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandSender;
import com.sk89q.minecraft.util.pagination.PaginatedResult;
import net.md_5.bungee.api.ChatColor;
import tc.oc.commons.core.chat.ChatUtils;

import java.util.List;

public abstract class PrettyPaginatedResult<T> extends PaginatedResult<T> {
    protected final String header;

    public PrettyPaginatedResult(String header) {
        this(header, 8);
    }

    public PrettyPaginatedResult(String header, int resultsPerPage) {
        super(resultsPerPage);
        this.header = Preconditions.checkNotNull(header, "header");
    }

    @Override
    public void display(WrappedCommandSender sender, List<? extends T> results, int page) throws CommandException {
        if(results.isEmpty()) {
            sender.sendMessage(formatEmpty());
        } else {
            super.display(sender, results, page);
        }
    }

    @Override
    public String formatHeader(int page, int totalPages) {
        ChatColor dashColor = ChatColor.BLUE;
        ChatColor textColor = ChatColor.DARK_AQUA;
        ChatColor highlight = ChatColor.AQUA;

        String message = this.header + textColor + " (" + highlight + page + textColor + " of " + highlight + totalPages + textColor + ")";
        return ChatUtils.horizontalLineHeading(message, dashColor, ChatUtils.MAX_CHAT_WIDTH);
    }

    public String formatEmpty() {
        return ChatColor.RED + "No results";
    }
}
