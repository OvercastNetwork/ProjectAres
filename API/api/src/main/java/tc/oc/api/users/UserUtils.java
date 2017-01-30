package tc.oc.api.users;

import tc.oc.commons.core.formatting.StringUtils;

public interface UserUtils {

    static String sanitizeUsername(String username) {
        return StringUtils.truncate(username.replaceAll("[^A-Za-z0-9_]", ""), 16);
    }
}
