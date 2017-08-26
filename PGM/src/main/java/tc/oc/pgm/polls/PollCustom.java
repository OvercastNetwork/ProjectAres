package tc.oc.pgm.polls;

import org.bukkit.Server;

public class PollCustom extends Poll {

    private String text;

    public PollCustom(PollManager pollManager, Server server, String initiator, String text) {
        super(pollManager, server, initiator);
        this.text = text;
    }

    @Override
    public void executeAction() {
        //I do nothing
    }

    @Override
    public String getActionString() {
        return boldAqua + text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    @Override
    public String getDescriptionMessage() {
        return boldAqua + text;
    }
}