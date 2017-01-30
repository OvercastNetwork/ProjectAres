package tc.oc.time;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.commons.core.util.TimeUtils;

public final class PeriodRenderers {
    private PeriodRenderers() {}

    public static PeriodRenderer colons() { return COLONS; }
    private static final PeriodRenderer COLONS = period -> new Component(String.format(
        "%s%02d:%02d%s",
        period.getUnits().contains(ChronoUnit.HOURS) ? period.get(ChronoUnit.HOURS) + ":" : "",
        TimeUtils.getUnitOrZero(period, ChronoUnit.MINUTES),
        TimeUtils.getUnitOrZero(period, ChronoUnit.SECONDS),
        period.getUnits().contains(ChronoUnit.MILLIS) ? String.format(".%03d", period.get(ChronoUnit.MILLIS)) : ""
    ));

    public static PeriodRenderer natural() { return NATURAL; }
    private static final PeriodRenderer NATURAL = period -> {
        if(period.getUnits().size() == 1) {
            final TemporalUnit unit = period.getUnits().get(0);
            return PeriodFormats.formatPeriod(period.get(unit), unit);
        } else {
            return Components.naturalList(
                period.getUnits()
                      .stream()
                      .map(unit -> PeriodFormats.formatPeriod(period.get(unit), unit))
            );
        }
    };
}
