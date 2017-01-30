package tc.oc.pgm.teams;

import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.SuggestException;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.pgm.match.inject.MatchScoped;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowFunction;
import static tc.oc.commons.core.stream.Collectors.toImmutableList;

@MatchScoped
public class TeamCommandUtils {

    private final Optional<TeamMatchModule> module;

    @Inject TeamCommandUtils(Optional<TeamMatchModule> module) {
        this.module = module;
    }

    public TeamMatchModule module() throws CommandException {
        return module
            .orElseThrow(() -> new TranslatableCommandException("command.noTeams"));
    }

    public Team team(String name) throws CommandException {
        return module.flatMap(tmm -> tmm.fuzzyMatch(name))
                     .orElseThrow(() -> new TranslatableCommandException("command.teamNotFound"));
    }

    public List<String> teamNames() throws CommandException {
        return module.map(tmm -> tmm.teams().map(Team::getName).collect(toImmutableList()))
                     .orElse(ImmutableList.of());
    }

    public List<String> teamNames(String prefix) throws CommandException {
        return StringUtils.complete(prefix, teamNames());
    }

    public Team teamArgument(CommandContext args, int index) throws CommandException, SuggestException {
        return team(args.string(index, teamNames()));
    }

    public Optional<Team> teamFlag(CommandContext args, char flag) throws CommandException, SuggestException {
        return args.tryFlag(flag, teamNames())
                   .map(rethrowFunction(this::team));
    }
}
