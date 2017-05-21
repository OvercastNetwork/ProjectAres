package tc.oc.pgm.polls;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.tokens.TokenUtil;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.mutation.MutationMatchModule;

public class PollMutation extends Poll {

    private Mutation mutation;
    private CommandSender sender;
    private MutationMatchModule module;
    private String mutationName;

    public PollMutation(PollManager pollManager, Server server, CommandSender sender, Mutation mutation,
                        MutationMatchModule module) {
        super(pollManager, server, sender.getName());
        this.mutation = mutation;
        this.sender = sender;
        this.module = module;
        this.mutationName = mutationName;
    }

    @Override
    public void executeAction() {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                "mutation enable -q " + mutation.name().toLowerCase());

        if (sender instanceof Player) {
            Player player = (Player) sender;
            TokenUtil.giveMutationTokens(TokenUtil.getUser(player), -1);
        }
    }

    @Override
    public String getActionString() {
        return normalize + "Add mutation: " + boldAqua + mutation.name().substring(0,1)
                + mutation.name().toLowerCase().substring(1);
    }

    @Override
    public String getDescriptionMessage() {
        return "to add the " + boldAqua + mutation.name().substring(0,1)
                + mutation.name().toLowerCase().substring(1) + " mutation";
    }
}
