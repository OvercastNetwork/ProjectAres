package tc.oc.api.minecraft.logging;

import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.logging.BetterRaven;

/**
 * Don't report other people's errors to us
 */
public class NotOurProblemRavenFilter implements PluginFacet {

    private static final Set<String> BLACKLIST = ImmutableSet.of(
        "com.sk89q.worldedit"
    );

    private final Optional<BetterRaven> raven;

    @Inject NotOurProblemRavenFilter(Optional<BetterRaven> raven) {
        this.raven = raven;
    }

    @Override
    public void enable() {
        raven.ifPresent(
            raven -> raven.addFilter(
                record -> BLACKLIST.stream().noneMatch(
                    prefix -> record.getLoggerName().startsWith(prefix)
                )
            )
        );
    }
}
