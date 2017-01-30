package tc.oc.pgm.mapratings;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.PGM;
import tc.oc.pgm.PGMTranslations;

public abstract class MapRatingsCommands {
    @Command(
        aliases = { "rate", "ratemap" },
        desc = "Rate the current map",
        usage = "[rating]",
        min = 0,
        max = 1
    )
    @CommandPermissions(MapRatingsMatchModule.RATE_PERM_NAME)
    public static void rate(CommandContext args, final CommandSender sender) throws CommandException {
        Integer score = null;
        if(args.argsLength() > 0) {
            score = args.getInteger(0);
        }

        final Player player = tc.oc.commons.bukkit.commands.CommandUtils.senderToPlayer(sender);

        final Match match = PGM.getMatchManager().getCurrentMatch(sender);
        if (match == null) {
            throw new CommandException(PGMTranslations.get().t("match.invalid", sender));
        }

        final MapRatingsMatchModule mrmm = match.getMatchModule(MapRatingsMatchModule.class);
        if(mrmm == null) {
            throw new CommandException(PGMTranslations.get().t("command.ratingsDisabled", sender));
        }

        if(score == null) {
            mrmm.showDialog(match.getPlayer(player));
        } else if(!mrmm.isScoreValid(score)) {
            throw new CommandException(PGMTranslations.get().t("command.rate.invalidRating", sender, mrmm.getMinimumScore(), mrmm.getMaximumScore()));
        } else {
            final Integer finalScore = score;
            mrmm.loadPlayerRating(match.getPlayer(player), new Runnable() {
                @Override
                public void run() {
                    mrmm.rate(match.getPlayer(player), finalScore);
                }
            });
        }
    }
}
