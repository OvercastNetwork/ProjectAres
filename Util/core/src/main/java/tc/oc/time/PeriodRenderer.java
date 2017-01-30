package tc.oc.time;

import java.time.temporal.TemporalAmount;

import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Renders a {@link TemporalAmount} to a {@link BaseComponent}
 */
public interface PeriodRenderer {
    BaseComponent renderPeriod(TemporalAmount period);
}

