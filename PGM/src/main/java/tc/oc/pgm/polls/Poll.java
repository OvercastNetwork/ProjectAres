package tc.oc.pgm.polls;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import tc.oc.commons.core.chat.Components;

import java.util.HashMap;
import java.util.Map;

public abstract class Poll implements Runnable {
    protected final Map<String, Boolean> votes = new HashMap<String, Boolean>();
    protected final long startTime = System.currentTimeMillis();
    protected final PollManager pollManager;
    protected final Server server;
    protected String initiator;
    protected int timeLeftSeconds;

    public static String boldAqua = ChatColor.BOLD + "" + ChatColor.AQUA;
    public static String normalize = ChatColor.RESET + "" + ChatColor.DARK_AQUA;
    public static String seperator = ChatColor.RESET + " | ";

    public Poll(PollManager pollManager, Server server, String initiator) {
        this.pollManager = pollManager;
        this.server = server;
        this.initiator = initiator;
        this.vote(true, initiator);
        timeLeftSeconds = 60;
    }

    public String getInitiator() {
        return this.initiator;
    }

    public int getTotalVotes() {
        return this.votes.size();
    }

    public int getVotesFor() {
        return this.getVotes(true);
    }

    public int getVotesAgainst() {
        return this.getVotes(false);
    }

    private int getVotes(boolean filterValue) {
        int total = 0;
        for(boolean vote : this.votes.values()) {
            if(vote == filterValue) total += 1;
        }
        return total;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public int getTimeLeftSeconds() {
        return timeLeftSeconds;
    }

    private void decrementTimeLeft() {
        timeLeftSeconds -= 5;
    }

    public boolean isSuccessful() {
        return this.getVotesFor() > this.getVotesAgainst();
    }

    public abstract void executeAction();

    public abstract String getActionString();

    public abstract String getDescriptionMessage();

    public String getStatusMessage() {
        String message = boldAqua + "[Poll] " + this.getTimeLeftSeconds() + normalize + " seconds left" + seperator;
        message += getActionString() + seperator + formatForAgainst();

        return message;
    }

    protected String formatForAgainst() {
        return normalize + "Yes: " + boldAqua + this.getVotesFor() + " " + normalize + "No: " + boldAqua + this.getVotesAgainst();
    }

    public static BaseComponent tutorialMessage() {
        BaseComponent yes = Components.clickEvent(new TextComponent("YES"), ClickEvent.Action.RUN_COMMAND, "/vote yes");
        yes = Components.hoverEvent(yes, HoverEvent.Action.SHOW_TEXT, new TextComponent("Vote in favor of the poll."));
        yes.setColor(net.md_5.bungee.api.ChatColor.GREEN);

        BaseComponent no = Components.clickEvent(new TextComponent("NO"), ClickEvent.Action.RUN_COMMAND, "/vote no");
        no = Components.hoverEvent(no, HoverEvent.Action.SHOW_TEXT, new TextComponent("Vote against the poll."));
        no.setColor(net.md_5.bungee.api.ChatColor.RED);

        return new TextComponent(
                Components.color(new TextComponent("Click "), net.md_5.bungee.api.ChatColor.DARK_AQUA),
                yes,
                Components.color(new TextComponent(" or "), net.md_5.bungee.api.ChatColor.DARK_AQUA),
                no,
                Components.color(new TextComponent(" to vote!"), net.md_5.bungee.api.ChatColor.DARK_AQUA)
        );
    }

    public boolean hasVoted(String playerName) {
        return this.votes.containsKey(playerName);
    }

    public void vote(boolean yes, String playerName) {
        this.votes.put(playerName, yes);
    }

    @Override
    public void run() {
        int timeLeftSeconds = this.getTimeLeftSeconds();
        if(timeLeftSeconds <= 0) {
            this.pollManager.endPoll(PollEndReason.Completed);
        } else if(timeLeftSeconds % 15 == 0 || (timeLeftSeconds < 15 && timeLeftSeconds % 5 == 0)) {
            this.server.broadcastMessage(this.getStatusMessage());
            this.server.broadcast(tutorialMessage());
        }
        this.decrementTimeLeft();
    }
}
