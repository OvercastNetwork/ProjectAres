package tc.oc.commons.bukkit.punishment;

import javax.annotation.Nullable;
import tc.oc.api.docs.virtual.PunishmentDoc;

public interface PunishmentPermissions {

    String BASE = "ocn.punishments";
        String LOOK_UP = BASE + ".lookup";
            String LOOK_UP_STALE = LOOK_UP + ".stale";
        String PUNISH = BASE + ".punish";
            String PUNISH_AUTO = PUNISH + ".auto";
            String PUNISH_SILENT = PUNISH + ".silent";
            String PUNISH_OFF_RECORD = PUNISH + ".off_record";
            String PUNISH_TIME = PUNISH + ".time";

    static String fromFlag(char flag) {
        switch(flag) {
            case 'p': return PUNISH_AUTO;
            case 's': return PUNISH_SILENT;
            case 'o': return PUNISH_OFF_RECORD;
            case 't': return PUNISH_TIME;
            default:  return "null";
        }
    }

    static String fromType(@Nullable PunishmentDoc.Type type) {
        return type == null ? PUNISH : BASE + "." + type.permission();
    }

}
