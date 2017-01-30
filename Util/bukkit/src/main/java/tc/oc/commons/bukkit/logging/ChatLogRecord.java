package tc.oc.commons.bukkit.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import net.md_5.bungee.api.ChatColor;
import tc.oc.commons.core.logging.Logging;

/**
 * A {@link LogRecord} with extra formatting features used by {@link ChatLogHandler}
 */
public class ChatLogRecord extends LogRecord {

    public ChatLogRecord(Level level, String msg) {
        super(level, msg);
    }

    /**
     * Return a colorful error message using legacy formatting codes
     */
    public String getLegacyFormattedMessage() {
        return ChatColor.DARK_GRAY + "[" +
               Logging.levelColor(getLevel()) +
               Logging.levelAbbrev(getLevel()) +
               ChatColor.DARK_GRAY + "] " +
               ChatColor.GRAY + getMessage();
    }

    /**
     * Returns true when it is not necessary to show a stack trace
     * for any throwable that might be in the record.
     */
    public boolean suppressStackTrace() {
        return false;
    }
}
