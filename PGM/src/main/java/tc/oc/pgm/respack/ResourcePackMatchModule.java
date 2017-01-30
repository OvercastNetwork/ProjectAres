package tc.oc.pgm.respack;

import javax.inject.Inject;

import tc.oc.commons.bukkit.respack.ResourcePackManager;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.module.ModuleDescription;

@ModuleDescription(name = "Resource Pack")
public class ResourcePackMatchModule extends MatchModule {

    private final ResourcePackManager resourcePackManager;

    @Inject ResourcePackMatchModule(Match match, ResourcePackManager resourcePackManager) {
        super(match);
        this.resourcePackManager = resourcePackManager;
    }

    @Override
    public void load() {
        super.load();
        resourcePackManager.refreshAll();
    }
}
