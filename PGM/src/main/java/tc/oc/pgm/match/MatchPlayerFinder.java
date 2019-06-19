package tc.oc.pgm.match;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import tc.oc.api.docs.UserId;

public interface MatchPlayerFinder {

    @Nullable MatchPlayer getPlayer(@Nullable UUID uuid);

    @Nullable MatchPlayer getPlayer(@Nullable UserId userId);

    @Nullable MatchPlayer getPlayer(@Nullable Player bukkit);

    default @Nullable MatchPlayer getPlayer(@Nullable Entity bukkit) {
        return bukkit instanceof Player ? this.getPlayer((Player) bukkit) : null;
    }

    default @Nullable MatchPlayer getPlayer(@Nullable CommandSender bukkit) {
        return bukkit instanceof Player ? this.getPlayer((Player) bukkit) : null;
    }

    default  @Nullable MatchPlayer getPlayer(@Nullable OfflinePlayer player) {
        return player != null && player.isOnline() ? getPlayer(player.getPlayer()) : null;
    }

    default  @Nullable MatchPlayer getPlayer(@Nullable MatchPlayerState state) {
        return state == null ? null : getPlayer(state.getUniqueId());
    }

    default Optional<MatchPlayer> player(@Nullable tc.oc.minecraft.api.entity.Player api) {
        return Optional.ofNullable(getPlayer((Player) api));
    }

    default Optional<MatchPlayer> player(@Nullable Player bukkit) {
        return Optional.ofNullable(getPlayer(bukkit));
    }

    default Optional<MatchPlayer> player(@Nullable Entity bukkit) {
        return Optional.ofNullable(getPlayer(bukkit));
    }

    default Optional<MatchPlayer> player(@Nullable CommandSender bukkit) {
        return Optional.ofNullable(getPlayer(bukkit));
    }

    default Optional<MatchPlayer> player(@Nullable UUID uuid) {
        return Optional.ofNullable(getPlayer(uuid));
    }

    default Optional<MatchPlayer> player(@Nullable UserId userId) {
        return Optional.ofNullable(getPlayer(userId));
    }

    default Optional<MatchPlayer> participant(@Nullable tc.oc.minecraft.api.entity.Player api) {
        return player(api).filter(MatchPlayer::isParticipating);
    }

    default Optional<MatchPlayer> participant(@Nullable Entity entity) {
        return player(entity).filter(MatchPlayer::isParticipating);
    }

    default Optional<MatchPlayer> participant(@Nullable UserId userId) {
        return player(userId).filter(MatchPlayer::isParticipating);
    }

    default Optional<MatchPlayer> interactor(@Nullable Entity bukkit) {
        return player(bukkit).filter(MatchPlayer::canInteract);
    }

    default @Nullable MatchPlayer getParticipant(@Nullable Entity entity) {
        return participant(entity).orElse(null);
    }

    default Optional<ParticipantState> participantState(@Nullable Entity entity) {
        return player(entity).flatMap(MatchPlayer::participantState);
    }

    default Optional<ParticipantState> participantState(@Nullable UserId userId) {
        return player(userId).flatMap(MatchPlayer::participantState);
    }

    default @Nullable ParticipantState getParticipantState(@Nullable Entity entity) {
        return participantState(entity).orElse(null);
    }

    default @Nullable ParticipantState getParticipantState(@Nullable UserId userId) {
        return participantState(userId).orElse(null);
    }

    default boolean canInteract(@Nullable MatchPlayer player) {
        return player != null && player.canInteract();
    }

    default boolean canInteract(@Nullable Player bukkit) {
        return canInteract(getPlayer(bukkit));
    }

    default boolean canInteract(@Nullable Entity bukkit) {
        return !(bukkit instanceof Player) || canInteract((Player) bukkit); // Assume all non-player entities can interact
    }

    default boolean canInteract(@Nullable MatchPlayerState player) {
        return player != null && player.canInteract();
    }
}
