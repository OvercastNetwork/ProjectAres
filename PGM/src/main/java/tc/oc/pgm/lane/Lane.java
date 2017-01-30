package tc.oc.pgm.lane;

import java.util.List;

import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.xml.finder.AllChildren;

@FeatureInfo(name = "lane")
public interface Lane extends FeatureDefinition {

    @Property
    TeamFactory team();

    @Property
    @Nodes(AllChildren.class)
    List<Region> regions();
}
