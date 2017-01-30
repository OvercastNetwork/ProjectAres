package tc.oc.commons.core.util;

import java.time.MonthDay;
import java.time.ZoneOffset;
import java.util.Set;

import com.google.common.collect.Sets;
import tc.oc.commons.core.stream.Collectors;

public interface Holidays {

    ZoneOffset ZONE = ZoneOffset.UTC;

    class Holiday {

        public final String key;
        public final MonthDay start;
        public final MonthDay end;

        Holiday(String key, MonthDay date) {
            this(key, date, date);
        }

        Holiday(String key, MonthDay start, MonthDay end) {
            this.key = key;
            this.start = start;
            this.end = end;
        }

        public boolean isNow() {
            final MonthDay now = MonthDay.now(ZONE);
            return !(start.isBefore(end) ? now.isBefore(start) || now.isAfter(end)
                                         : now.isBefore(start) && now.isAfter(end));
        }

    }

    Holiday APRIL_FOOLS = new Holiday(
        "april-fools",
        MonthDay.of(4, 1)
    );

    Holiday HALLOWEEN = new Holiday(
        "halloween",
        MonthDay.of(10, 1),
        MonthDay.of(10, 31)
    );

    Holiday CHRISTMAS = new Holiday(
            "christmas",
            MonthDay.of(12, 1),
            MonthDay.of(12, 26)
    );

    static Set<Holiday> all() {
        return Sets.newHashSet(APRIL_FOOLS, HALLOWEEN, CHRISTMAS);
    }

    static Set<String> keys() {
        return all().stream().map(h -> h.key).collect(Collectors.toImmutableSet());
    }

}
