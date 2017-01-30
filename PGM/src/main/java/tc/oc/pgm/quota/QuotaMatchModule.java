package tc.oc.pgm.quota;

import java.util.Collection;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import java.time.Duration;
import java.time.Instant;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.commons.bukkit.chat.Links;
import tc.oc.pgm.join.JoinDenied;
import tc.oc.pgm.join.JoinHandler;
import tc.oc.pgm.join.JoinMatchModule;
import tc.oc.pgm.join.JoinMethod;
import tc.oc.pgm.join.JoinRequest;
import tc.oc.pgm.join.JoinResult;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.module.ModuleDescription;

@ModuleDescription(name = "Match Quota")
public class QuotaMatchModule extends MatchModule implements JoinHandler {

    class QuotaStatus {
        final Quota quota;
        final Instant now;
        int matchesPlayed;
        Instant earliestJoinTime;

        QuotaStatus(Quota quota, Instant now) {
            this.quota = quota;
            this.now = now;
        }

        int matchesRemaining() {
            return Math.max(0, quota.maximum() - matchesPlayed);
        }

        Collection<BaseComponent> format() {
            final ImmutableList.Builder<BaseComponent> lines = ImmutableList.builder();

            lines.add(new TranslatableComponent(
                "matchQuota.matchCounts",
                new Component(String.valueOf(matchesPlayed), ChatColor.AQUA),
                new Component(String.valueOf(quota.maximum()), ChatColor.AQUA)
            ));

            if(matchesRemaining() == 0) {
                lines.add(new TranslatableComponent(
                    "matchQuota.nextMatch",
                    new Component(PeriodFormats.briefNaturalApproximate(now, earliestJoinTime), ChatColor.AQUA)
                ));
            }

            if(!quota.premium()) {
                Range<Integer> premiumRange = getConfig().getPremiumMaximum();
                if(premiumRange != null) {
                    if(premiumRange.upperEndpoint() == Integer.MAX_VALUE) {
                        lines.add(Links.shopPlug("shop.plug.rankedMatches.unlimited"));
                    } else {
                        BaseComponent premiumLimit = new Component(String.valueOf(premiumRange.upperEndpoint()), ChatColor.AQUA);
                        if(premiumRange.upperEndpoint().equals(premiumRange.lowerEndpoint())) {
                            lines.add(Links.shopPlug("shop.plug.rankedMatches.uniform", premiumLimit));
                        } else {
                            lines.add(Links.shopPlug("shop.plug.rankedMatches.upto", premiumLimit));
                        }
                    }
                }
            }

            return lines.build();
        }
    }

    private final QuotaConfig config;

    @Inject QuotaMatchModule(QuotaConfig config) {
        this.config = config;
    }

    @Override
    public void load() {
        super.load();
        getMatch().needMatchModule(JoinMatchModule.class).registerHandler(this);
    }

    public QuotaConfig getConfig() {
        return config;
    }

    public @Nullable Quota getQuota(MatchPlayer player) {
        for(Quota quota : getConfig().getQuotas()) {
            if(quota.appliesTo(player)) return quota;
        }
        return null;
    }

    public QuotaStatus getQuotaStatus(MatchPlayer player, Quota quota, Instant now) {
        Duration interval = quota.interval();

        QuotaStatus status = new QuotaStatus(quota, now);
        status.matchesPlayed = 0;
        status.earliestJoinTime = now;

        // Note that this list is reverse-chrono order
        for(Instant joinTime : player.recentMatchCommitments()) {
            Instant expireTime = joinTime.plus(interval);
            if(expireTime.isAfter(now)) {
                if(++status.matchesPlayed == quota.maximum()) {
                    status.earliestJoinTime = expireTime;
                }
            } else {
                break;
            }
        }

        return status;
    }

    public @Nullable QuotaStatus getQuotaStatus(MatchPlayer player) {
        Quota quota = getQuota(player);
        return quota == null ? null : getQuotaStatus(player, quota, Instant.now());
    }

    public void sendQuotaInfo(MatchPlayer player) {
        final QuotaStatus status = getQuotaStatus(player);
        if(status != null) {
            status.format().forEach(player::sendMessage);
        }
    }

    @Override
    public @Nullable JoinResult queryJoin(MatchPlayer joining, JoinRequest request) {
        if(request.method() == JoinMethod.USER && !getMatch().hasEverParticipated(joining.getPlayerId())) {
            final QuotaStatus status = getQuotaStatus(joining);
            if(status != null && status.matchesRemaining() <= 0) {
                return JoinDenied.error("matchQuota.outOfMatches")
                    .also(status.format());
            }
        }
        return null;
    }
}
