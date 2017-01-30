package tc.oc.commons.bukkit.chat;

/**
 * Individual traits that make up {@link NameStyle}s
 */
public enum NameFlag {
    COLOR,              // Color
    FLAIR,              // Show flair
    SELF,               // Bold if self
    FRIEND,             // Italic if friend
    DISGUISE,           // Strikethrough if disguised
    NICKNAME,           // Show nickname after real name
    DEATH,              // Grey out name if dead
    TELEPORT,           // Click name to teleport
    MAPMAKER;           // Show mapmaker flair
}
