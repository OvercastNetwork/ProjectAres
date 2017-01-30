package tc.oc.api.games;

import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import tc.oc.api.docs.Arena;
import tc.oc.api.model.ModelStore;

@Singleton
public class ArenaStore extends ModelStore<Arena> {

    private final SetMultimap<String, Arena> byDatacenter = HashMultimap.create();
    private final Table<String, String, Arena> byDatacenterAndGameId = HashBasedTable.create();

    public Set<Arena> byDatacenter(String datacenter) {
        return byDatacenter.get(datacenter);
    }

    public @Nullable Arena tryDatacenterAndGameId(String datacenter, String gameId) {
        return byDatacenterAndGameId.get(datacenter, gameId);
    }

    @Override
    protected void unindex(Arena doc) {
        super.unindex(doc);
        byDatacenter.remove(doc.datacenter(), doc);
        byDatacenterAndGameId.remove(doc.datacenter(), doc.game_id());
    }

    @Override
    protected void reindex(Arena doc) {
        super.reindex(doc);
        byDatacenter.put(doc.datacenter(), doc);
        byDatacenterAndGameId.put(doc.datacenter(), doc.game_id(), doc);
    }
}
