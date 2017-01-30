package tc.oc.pgm.skillreq;

import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.broadcast.Broadcast;
import tc.oc.pgm.join.JoinDenied;
import tc.oc.pgm.join.JoinHandler;
import tc.oc.pgm.join.JoinMatchModule;
import tc.oc.pgm.join.JoinMethod;
import tc.oc.pgm.join.JoinRequest;
import tc.oc.pgm.join.JoinResult;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.module.ModuleDescription;

@ModuleDescription(name = "Skill Requirement")
public class SkillRequirementMatchModule extends MatchModule implements JoinHandler {

    @Override
    public void load() {
        super.load();
        getMatch().needMatchModule(JoinMatchModule.class).registerHandler(this);
    }

    public int getMinimumKills() {
        return getMatch().getPlugin().getConfig().getInt("join.requirements.minimum-kills", 0);
    }

    @Override
    public @Nullable JoinResult queryJoin(MatchPlayer joining, JoinRequest request) {
        if(request.method() == JoinMethod.USER && !getMatch().hasEverParticipated(joining.getPlayerId())) {
            int remaining = getMinimumKills() - joining.getDocument().enemy_kills();
            if(remaining > 0) {
                return JoinDenied.error("skillRequirement.fail.kills", new Component(String.valueOf(remaining), ChatColor.AQUA))
                    .also(Broadcast.Type.TIP.format(new TranslatableComponent("skillRequirement.fail.general")));
            }
        }
        return null;
    }

    public void sendFeedback(MatchPlayer player) {
        final JoinResult result = queryJoin(player, new JoinRequest(JoinMethod.USER, null));
        if(result != null && !result.isAllowed()) {
            result.output().forEach(player::sendMessage);
        }
    }
}
