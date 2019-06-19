package tc.oc.pgm.spawns.states;

import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Location;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.events.ObserverInteractEvent;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.spawns.Spawn;

import static tc.oc.minecraft.protocol.MinecraftVersion.lessThan;
import static tc.oc.minecraft.protocol.MinecraftVersion.MINECRAFT_1_8;

/**
 * Player is waiting to spawn as a participant
 */
public abstract class Spawning extends Participating {

    protected boolean legacy;
    protected boolean spawnRequested;
    protected boolean seenTitle;

    public Spawning(MatchPlayer player) {
        super(player);
        this.legacy = lessThan(MINECRAFT_1_8, player.getBukkit().getProtocolVersion());
        this.spawnRequested = options.auto || legacy;
        this.seenTitle = false;
    }

    @Override
    public void enterState() {
        super.enterState();

        bukkit.setGravity(true);
        player.setDead(true);
        player.refreshInteraction();
        player.refreshVisibility();
    }

    public void requestSpawn() {
        this.spawnRequested = true;
    }

    @Override
    public void onEvent(ObserverInteractEvent event) {
        super.onEvent(event);
        requestSpawn();
    }

    @Override
    public void tick() {
        if(!trySpawn()) {
            updateTitle();
        }

        super.tick();
    }

    protected boolean trySpawn() {
        if(!spawnRequested) return false;

        Spawn spawn = chooseSpawn();
        if(spawn == null) return false;

        Location location = spawn.getSpawn(player);
        if(location == null) return false;

        transition(new Alive(player, spawn, location));
        return true;
    }

    public @Nullable Spawn chooseSpawn() {
        if(spawnRequested) {
            return smm.chooseSpawn(player);
        } else {
            return null;
        }
    }

    public void updateTitle() {
        if(legacy && seenTitle) return;

        player.showTitle(getTitle(), new Component(getSubtitle(), ChatColor.GREEN), 0, 3, 3);
        seenTitle = true;
    }

    protected abstract BaseComponent getTitle();

    protected BaseComponent getSubtitle() {
        if(!spawnRequested) {
            return new TranslatableComponent("death.respawn.unconfirmed");
        } else if(options.message != null) {
            return options.message;
        } else {
            return new TranslatableComponent("death.respawn.confirmed.waiting");
        }
    }
}
