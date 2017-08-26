package tc.oc.pgm.pollablemaps;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import tc.oc.pgm.Config;
import tc.oc.pgm.PGM;
import tc.oc.pgm.map.PGMMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PollableMaps {

    private List<PGMMap> maps;

    public PollableMaps() {
        maps = new ArrayList<PGMMap>();
        loadPollableMaps();
    }

    public List<PGMMap> getMaps() {
        return maps;
    }

    public void loadPollableMaps() {
        Path filepath = Config.Poll.getPollAbleMapPath();
        if (filepath == null) return;
        List<String> lines = null;
        try {
            lines = Files.readAllLines(filepath, Charsets.UTF_8);
        } catch (IOException e) {
            PGM.get().getLogger().severe("Error in reading pollable maps from file!");
        }
        if (lines == null) return;
        ImmutableList.Builder<PGMMap> maps = ImmutableList.builder();
        for(String line : lines) {
            line = line.trim();
            if(line.isEmpty()) {
                continue;
            }

            Optional<PGMMap> map = PGM.get().getMapLibrary().getMapByNameOrId(line);
            if(map.isPresent()) {
                maps.add(map.get());
            } else {
                PGM.get().getMapLibrary().getLogger().severe("Unknown map '" + line
                        + "' when parsing " + filepath.toString());
            }
        }
        this.maps = maps.build();
    }

    public boolean isAllowed(PGMMap map) {
        return !maps.contains(map);
    }
}
