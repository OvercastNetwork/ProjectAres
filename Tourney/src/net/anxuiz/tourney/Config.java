package net.anxuiz.tourney;

import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.configuration.Configuration;

public class Config {
    public static @Nullable String tournamentID() {
        return getConfiguration().getString("tournament-id", null);
    }

    public static int maxClassificationPlays() {
        return getConfiguration().getInt("max-classification-plays", 3);
    }

    public static int maxMapPlays() {
        return getConfiguration().getInt("max-map-plays", 3);
    }

    public static List<String> classificationMatchFamilies() {
        return getConfiguration().getStringList("classification-match-families");
    }

    public static List<String> mapMatchFamilies() {
        return getConfiguration().getStringList("map-match-families");
    }

    public static boolean betterSprintAllowed() {
        return getConfiguration().getBoolean("better-sprint-allowed", false);
    }

    public static Configuration getConfiguration() {
        return Tourney.get().getConfig();
    }
}
