package tc.oc.commons.bukkit.util;

import java.util.List;

public class ObjectUtils {

    public static <T> List<T> paginate(List<T> objects, int page, int pageLength) {
        if (objects.size() <= ((page - 1) * pageLength)) {
            return null;
        }
        int firstItem = ((page * pageLength) - pageLength);
        int lastItem = (firstItem + pageLength);
        int max = objects.size() <= lastItem ? objects.size() : lastItem;
        if (max < firstItem) {
            firstItem = max - 1;
        }
        return objects.subList(firstItem, max);
    }

}
