package tc.oc.pgm.analytics;

import org.apache.commons.io.FileUtils;
import tc.oc.api.minecraft.users.UserStore;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.scheduler.Scheduler;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.minecraft.api.entity.Player;
import tc.oc.minecraft.protocol.MinecraftVersion;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@Singleton
public class PlayerVersionLogger implements PluginFacet {

    private final UserStore<Player> userStore;
    private final Scheduler scheduler;

    private File playerVersionLogFile;
    private Task task;

    @Inject PlayerVersionLogger(UserStore<Player> userStore, Scheduler scheduler) {
        this.userStore = userStore;
        this.scheduler = scheduler;

        playerVersionLogFile = new File("player_versions.log");
    }

    @Override
    public void enable() {
        task = this.scheduler.createRepeatingTask(Duration.ofMinutes(15), Duration.ofMinutes(15), this::logPlayerVersions);
    }

    @Override
    public void disable() {
        task.cancel();
    }

    public void logPlayerVersions() {
        Map<String, Integer> playerCountVersionMap = new HashMap<>();
        IntStream.rangeClosed(7, 12).forEach(v -> playerCountVersionMap.put("1." + v, 0));
        userStore.stream().forEach(player -> {
            String version = MinecraftVersion.describeProtocol(player.getProtocolVersion(), true);
            playerCountVersionMap.put(version, playerCountVersionMap.getOrDefault(version, 0) + 1);
        });

        StringBuilder builder = new StringBuilder();
        builder.append("[").append(new Date().getTime());

        playerCountVersionMap.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey().split("\\.")[1])))
                .forEach( entry -> builder.append(", ").append(entry.getValue()));
        builder.append("]\n");

        try {
            FileUtils.writeStringToFile(playerVersionLogFile, builder.toString(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
