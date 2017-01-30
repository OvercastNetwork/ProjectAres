package tc.oc.pgm.ffa;

public class FreeForAllOptions {
    public final int minPlayers;
    public final int maxPlayers;
    public final int maxOverfill;
    public final org.bukkit.scoreboard.Team.OptionStatus nameTagVisibility;
    public final boolean colors;

    public FreeForAllOptions(int minPlayers, int maxPlayers, int maxOverfill, org.bukkit.scoreboard.Team.OptionStatus nameTagVisibility, boolean colors) {
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.maxOverfill = maxOverfill;
        this.nameTagVisibility = nameTagVisibility;
        this.colors = colors;
    }
}
