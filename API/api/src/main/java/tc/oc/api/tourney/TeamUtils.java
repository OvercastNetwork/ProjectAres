package tc.oc.api.tourney;

public final class TeamUtils {
    private TeamUtils() {}

    public static String normalizeName(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
