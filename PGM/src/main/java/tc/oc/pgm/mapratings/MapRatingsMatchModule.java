package tc.oc.pgm.mapratings;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;

import me.anxuiz.settings.bukkit.PlayerSettings;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import org.bukkit.permissions.Permission;
import tc.oc.api.docs.MapRating;
import tc.oc.api.docs.UserId;
import tc.oc.api.maps.MapRatingsRequest;
import tc.oc.api.maps.MapService;
import tc.oc.commons.bukkit.event.ObserverKitApplyEvent;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PlayerLeaveMatchEvent;
import tc.oc.pgm.blitz.BlitzMatchModule;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchExecutor;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerExecutor;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.MultiPlayerParty;
import tc.oc.pgm.settings.Settings;

@ListenerScope(MatchScope.LOADED)
public class MapRatingsMatchModule extends MatchModule implements Listener {
    public static final String RATE_PERM_NAME = "map.rating.rate";
    public static final Permission RATE_PERM = new Permission(RATE_PERM_NAME);
    public static final Permission VIEW_LIVE_PERM = new Permission("map.rating.view.live");

    // Player is allowed to rate if the match has ended and they participated at least this much
    private static final double MIN_PARTICIPATION_PERCENT = 0.75f;

    // Player is allowed to rate if they participated for this long, regardless of match state
    private static final Duration MIN_PARTICIPATION_TIME = Duration.ofMinutes(10);

    // Time between the end of the match and automatically showing the rating dialog
    // This is synced with the match end title
    private static final Duration DIALOG_DELAY = Duration.ofSeconds(4);

    // Inventory slot for the button that opens the rating dialog
    private static final int OPEN_BUTTON_SLOT = 5;

    // Magic invisible string used to identify items serving as buttons
    private static final String BUTTON_PREFIX = ChatColor.COLOR_CHAR + "z";

    private static final DyeColor[] BUTTON_COLORS = new DyeColor[] {
        DyeColor.RED,
        DyeColor.PURPLE,
        DyeColor.BLUE,
        DyeColor.CYAN,
        DyeColor.LIME
    };

    private static final String[] BUTTON_LABELS = new String[] {
        "rating.choice.terrible",
        "rating.choice.bad",
        "rating.choice.ok",
        "rating.choice.good",
        "rating.choice.amazing"
    };

    private static final ChatColor[] BUTTON_LABEL_COLORS = new ChatColor[] {
        ChatColor.RED,
        ChatColor.LIGHT_PURPLE,
        ChatColor.BLUE,
        ChatColor.AQUA,
        ChatColor.GREEN
    };

    @Inject private MapRatingsConfiguration config;
    @Inject private MapService mapService;
    @Inject private MatchExecutor matchExecutor;
    @Inject private BlitzMatchModule blitz;

    private final Map<MatchPlayer, Integer> playerRatings = new HashMap<>();

    private final int minimumScore, maximumScore;

    @Inject MapRatingsMatchModule(Match match) {
        super(match);

        this.minimumScore = 1;
        this.maximumScore = 5;
    }

    @Override
    public boolean shouldLoad() {
        return config.enabled();
    }

    private static String formatScore(@Nullable Integer score) {
        return score == null ? "" : BUTTON_LABEL_COLORS[score - 1].toString() + ChatColor.BOLD + score;
    }

    public int getMinimumScore() {
        return minimumScore;
    }

    public int getMaximumScore() {
        return maximumScore;
    }

    public boolean isScoreValid(int score) {
        return score >= minimumScore && score <= maximumScore;
    }

    /**
     * Return a friendly description of why the given player is not allowed to rate maps,
     * or null if they are allowed.
     */
    public @Nullable String cantRateReason(MatchPlayer player) {
        if(!player.getBukkit().hasPermission(RATE_PERM)) {
            return PGMTranslations.t("noPermission", player);
        }

        if(Comparables.lessThan(player.getCumulativeParticipationTime(), MIN_PARTICIPATION_TIME) &&
           !(blitz.eliminated(player)) &&
           !(this.getMatch().isFinished() && player.getCumulativeParticipationPercent() > MIN_PARTICIPATION_PERCENT)) {
            return PGMTranslations.t("rating.lowParticipation", player);
        }

        return null;
    }

    public @Nullable String cantShowDialogReason(MatchPlayer player) {
        if(player.isParticipating()) {
            return PGMTranslations.get().t(
                ChatColor.RED.toString(),
                "rating.whilePlaying",
                player.getBukkit(),
                ChatColor.GOLD + "/rate " + ChatColor.ITALIC + minimumScore + "..." + maximumScore
            );
        }
        return this.cantRateReason(player);
    }

    public boolean canRate(MatchPlayer player) {
        return this.cantRateReason(player) == null;
    }

    public boolean canShowDialog(MatchPlayer player) {
        return this.cantShowDialogReason(player) == null;
    }

    public boolean checkCanRate(MatchPlayer player) {
        String reason = this.cantRateReason(player);
        if(reason == null) return true;

        player.sendWarning(reason, false);
        return false;
    }

    public boolean checkCanShowDialog(MatchPlayer player) {
        String reason = this.cantShowDialogReason(player);
        if(reason == null) return true;

        player.sendWarning(reason, false);
        return false;
    }

    private ItemStack getOpenButton(MatchPlayer player) {
        ItemStack stack = new ItemStack(Material.HOPPER);
        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName(BUTTON_PREFIX + ChatColor.BLUE.toString() + ChatColor.BOLD + PGMTranslations.t("rating.rateThisMap", player));
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack getScoreButton(MatchPlayer player, int i) {
        Integer score = this.playerRatings.get(player);

        MaterialData material;
        if(score != null && score == i + 1) {
            material = new MaterialData(Material.CARPET, BUTTON_COLORS[i].getWoolData());
        } else {
            material = new Wool(BUTTON_COLORS[i]);
        }
        ItemStack stack = material.toItemStack(i + 1);

        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName(BUTTON_PREFIX + BUTTON_LABEL_COLORS[i] + ChatColor.BOLD + PGMTranslations.t(BUTTON_LABELS[i], player));
        stack.setItemMeta(meta);

        return stack;
    }

    private void updateScoreButtons(MatchPlayer player, Inventory inv) {
        for(int i = minimumScore - 1; i < maximumScore; i++) {
            inv.setItem(2 + i, this.getScoreButton(player, i));
        }
    }

    public void showDialog(final MatchPlayer player) {
        if(!checkCanShowDialog(player)) return;

        this.loadPlayerRating(player, () -> {
            String title = ChatColor.DARK_BLUE.toString() + ChatColor.BOLD + PGMTranslations.t("rating.rateThisMap", player);
            Inventory inv = getMatch().getServer().createInventory(
                    player.getBukkit(),
                    9,
                    StringUtils.truncate(title, 32)
            );
            updateScoreButtons(player, inv);
            player.getBukkit().openInventory(inv);
        });
    }

    public void rate(final MatchPlayer player, final int score) {
        if(!this.checkCanRate(player)) return;

        final Integer oldScore = this.playerRatings.put(player, score);

        InventoryView inv = player.getBukkit().getOpenInventory();
        if(inv.getTopInventory().getType() == InventoryType.HOPPER) {
            this.updateScoreButtons(player, inv.getTopInventory());
        }

        if(oldScore != null && score == oldScore) {
            player.sendWarning(PGMTranslations.t("rating.sameRating", player, score), false);
            return;
        }

        player.facet(MatchPlayerExecutor.class).callback(mapService.rate(new MapRating(
            player.getPlayerId(),
            getMatch().getMap().getDocument(),
            score,
            null
        )), CommandFutureCallback.onSuccess(player.getBukkit(), result -> {
            if(result.first.isOnline()) {
                result.first.getBukkit().closeInventory();
            }
            notifyRating(result.first, score, oldScore);
        }));
    }

    private void notifyRating(MatchPlayer rater, int score, @Nullable Integer oldScore) {
        rater.sendMessage(PGMTranslations.get().t(
            ChatColor.WHITE.toString(),
            oldScore == null ? "command.rate.successful" : "command.rate.update",
            rater.getBukkit(),
            formatScore(score),
            this.getMatch().getMapInfo().getColoredName(),
            this.getMatch().getMapInfo().getColoredVersion(),
            formatScore(oldScore)
        ));

        if(oldScore == null) {
            rater.sendMessage(PGMTranslations.get().t(
                ChatColor.BLUE.toString(),
                "rating.changeLater",
                rater.getBukkit(),
                ChatColor.GOLD + "/rate"
            ));
        }

        for(MatchPlayer viewer : this.getMatch().getPlayers()) {
            if(viewer != rater && viewer.getBukkit().hasPermission(VIEW_LIVE_PERM)) {
                String message;
                if(rater.getParty() instanceof MultiPlayerParty) {
                    viewer.sendMessage(PGMTranslations.get().t(
                        ChatColor.WHITE.toString(),
                        oldScore == null ? "rating.create.notify" : "rating.update.notify",
                        viewer.getBukkit(),
                        rater.getParty().getColoredName(),
                        formatScore(score),
                        formatScore(oldScore)
                    ));
                } else {
                    viewer.sendMessage(PGMTranslations.get().t(
                        ChatColor.WHITE.toString(),
                        oldScore == null ? "rating.create.notify.ffa" : "rating.update.notify.ffa",
                        viewer.getBukkit(),
                        formatScore(score),
                        formatScore(oldScore)
                    ));
                }
            }
        }
    }

    @EventHandler
    public void onOpenButtonClick(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        MatchPlayer player = this.getMatch().getPlayer(event.getPlayer());
        if(player == null) return;

        ItemStack stack = event.getPlayer().getItemInHand();
        if(stack == null) return;
        if(stack.getType() != Material.HOPPER) return;

        String name = stack.getItemMeta().getDisplayName();
        if(name == null || !name.startsWith(BUTTON_PREFIX)) return;
        this.showDialog(player);
    }

    @EventHandler
    public void onButtonClick(final InventoryClickEvent event) {
        ItemStack stack = event.getCurrentItem();
        final MatchPlayer player = this.getMatch().getPlayer(event.getWhoClicked());

        if(stack == null || player == null) return;
        if(stack.getType() != Material.WOOL && stack.getType() != Material.CARPET) return;
        ItemMeta meta = stack.getItemMeta();
        if(!meta.hasDisplayName()) return;
        String name = meta.getDisplayName();
        if(!name.startsWith(BUTTON_PREFIX)) return;

        event.setCancelled(true);

        final int score = stack.getAmount();
        if(!isScoreValid(score)) return;

        this.getMatch().getScheduler(MatchScope.LOADED).createTask(() -> {
            Integer oldScore = playerRatings.get(player);
            if(oldScore == null || oldScore != score) {
                player.playSound(Sound.UI_BUTTON_CLICK, 1, 2);
                rate(player, score);
            }
            else {
                player.getBukkit().closeInventory();
            }
        });
    }

    protected void loadPlayerRating(final MatchPlayer player, final @Nullable Runnable callback) {
        if(this.playerRatings.containsKey(player)) {
            if(callback != null) callback.run();
            return;
        }

        matchExecutor.callback(
            mapService.getRatings(new MapRatingsRequest(
                getMatch().getMap().getDocument(),
                Collections.singletonList(player.getPlayerId())
            )),
            (match, result) -> {
                if(player.isOnline()) {
                    Integer score = result.player_ratings().get(player.getPlayerId());
                    playerRatings.put(player, score);
                    if(callback != null)
                        callback.run();
                }
            }
        );
    }

    @Override
    public void disable() {
        super.disable();

        this.getMatch().getScheduler(MatchScope.LOADED).createDelayedTask(DIALOG_DELAY, () -> {
            loadAllPlayerRatings(this::showDialogToAll);
        });
    }

    private void showDialogToAll() {
        for(MatchPlayer player : this.getMatch().getPlayers()) {
            if(this.canRate(player) &&
               this.playerRatings.get(player) == null &&
               PlayerSettings.getManager(player.getBukkit()).getValue(Settings.RATINGS, Boolean.class)) {

                this.showDialog(player);
            }
        }
    }

    private void loadAllPlayerRatings(final Runnable callback) {
        List<UserId> userIds = new ArrayList<>();

        for(MatchPlayer player : this.getMatch().getPlayers()) {
            if(this.canRate(player)) {
                userIds.add(player.getPlayerId());
            }
        }

        matchExecutor.callback(
            mapService.getRatings(new MapRatingsRequest(
                getMatch().getMap().getDocument(),
                userIds
            )),
            (match, ratings) -> {
                for(MatchPlayer player : getMatch().getPlayers()) {
                    playerRatings.put(player, ratings.player_ratings().get(player.getPlayerId()));
                }

                if(callback != null) callback.run();
            }
        );
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveMatchEvent event) {
        this.playerRatings.remove(event.getPlayer());
    }

    @EventHandler
    public void giveKit(ObserverKitApplyEvent event) {
        match.player(event.getPlayer())
             .filter(player -> player.isObserving() && canShowDialog(player))
             .ifPresent(player -> player.getInventory().setItem(OPEN_BUTTON_SLOT, getOpenButton(player)));
    }
}
