package tc.oc.pgm.spawns.states;

import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.event.inventory.InventoryClickEvent;
import tc.oc.commons.bukkit.freeze.FrozenPlayer;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.spawns.Spawn;
import tc.oc.pgm.spawns.SpawnModule;
import tc.oc.pgm.spawns.events.DeathKitApplyEvent;

/**
 * Player is waiting to respawn after dying in-game
 */
public class Dead extends Spawning {
    private static final long CORPSE_ROT_TICKS = 15;

    private final long deathTick;
    private boolean kitted, rotted;
    private @Nullable FrozenPlayer frozenPlayer;

    public Dead(MatchPlayer player) {
        this(player, player.getMatch().getClock().now().tick);
    }

    public Dead(MatchPlayer player, long deathTick) {
        super(player);
        this.deathTick = deathTick;
    }

    @Override
    public void enterState() {
        super.enterState();

        player.clearInventory();
        bukkit.setGravity(true);

        if(player.isVisible()) NMSHacks.playDeathAnimation(player.getBukkit());

        if(!options.spectate) {
            frozenPlayer = freezer.freeze(bukkit);
        }

        // Show red vignette
        NMSHacks.showBorderWarning(player.getBukkit(), true);

        // Flash/wobble the screen. If we don't delay this then the client glitches out
        // when the player dies from a potion effect. I have no idea why it happens,
        // but this fixes it. We could investigate a better fix at some point.
        smm.getMatch().getScheduler(MatchScope.LOADED).createTask(() -> {
            if(isCurrent() && bukkit.isOnline()) {
                NMSHacks.sendDeathEffects(bukkit, options.blackout);
            }
        });
    }

    @Override
    public void leaveState() {
        if(frozenPlayer != null) {
            frozenPlayer.thaw();
            frozenPlayer = null;
        }
        player.setDead(false);
        NMSHacks.showBorderWarning(bukkit, false);
        NMSHacks.clearDeathEffects(bukkit);

        super.leaveState();
    }

    protected long age() {
        return player.getMatch().getClock().now().tick - deathTick;
    }

    @Override
    public void tick() {
        long age = age();

        if(!kitted && ticksUntilRespawn() <= 0) {
            this.kitted = true;
            // Give the player the team/class picker, after death has cleared their inventory
            player.getMatch().callEvent(new DeathKitApplyEvent(player));
            bukkit.updateInventory();
        }

        if(!rotted && age >= CORPSE_ROT_TICKS) {
            this.rotted = true;
            // Make player invisible after the death animation is complete
            player.setVisible(false);
            player.refreshVisibility();
        }

        super.tick(); // May transition to a different state, so call last
    }

    @Override
    public void onEvent(PlayerChangePartyEvent event) {
        super.onEvent(event);
        if(!(event.getNewParty() instanceof Competitor)) {
            transition(new Observing(player, true, false));
        }
    }

    protected long ticksUntilRespawn() {
        return Math.max(0, options.delayTicks - age());
    }

    @Override
    public @Nullable Spawn chooseSpawn() {
        if(ticksUntilRespawn() > 0) {
            return null;
        } else {
            return super.chooseSpawn();
        }
    }

    public void requestSpawn() {
        if(player.getMatch().getClock().now().tick - deathTick >= SpawnModule.IGNORE_CLICKS_DELAY.toMillis() / 50) {
            super.requestSpawn();
        }
    }

    @Override
    protected BaseComponent getTitle() {
        BaseComponent title = new TranslatableComponent("deathScreen.title");
        title.setColor(ChatColor.RED);
        return title;
    }

    @Override
    protected BaseComponent getSubtitle() {
        long ticks = ticksUntilRespawn();
        if(ticks > 0) {
            return new TranslatableComponent(spawnRequested ? "death.respawn.confirmed.time"
                                                            : "death.respawn.unconfirmed.time",
                                             new Component(String.format("%.1f", (ticks / (float) 20))).color(ChatColor.AQUA));
        } else {
            return super.getSubtitle();
        }
    }

    @Override
    public void onEvent(InventoryClickEvent event) {
        super.onEvent(event);
        event.setCancelled(true); // don't allow inventory changes when dead
    }
}
