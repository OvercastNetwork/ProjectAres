package tc.oc.pgm.match;

import javax.inject.Inject;

import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.module.ModuleContext;

public class MatchModuleContext extends ModuleContext<MatchModule, MatchScoped> {

    private final Match match;

    @Inject MatchModuleContext(Match match) {
        this.match = match;
    }

    @Override
    public void load() {
        asCurrentScope(() -> {
            super.load();
            loadedModules().forEach(matchModule -> {
                // It's important that we register events immediately after calling load(),
                // because modules that listen for new players or parties will expect
                // them to either be present at load time, or to generate an event that the
                // module can catch later.
                matchModule.load();
                match.registerEventsAndRepeatables(matchModule);
            });
        });
    }
}
