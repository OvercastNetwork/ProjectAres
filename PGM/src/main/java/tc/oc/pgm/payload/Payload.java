package tc.oc.pgm.payload;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Minecart;
import org.bukkit.event.HandlerList;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Rails;
import org.bukkit.util.Vector;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.util.Comparables;
import tc.oc.commons.core.util.DefaultMapAdapter;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.goals.OwnedGoal;
import tc.oc.pgm.goals.SimpleGoal;
import tc.oc.pgm.goals.events.GoalCompleteEvent;
import tc.oc.pgm.goals.events.GoalStatusChangeEvent;
import tc.oc.pgm.match.*;
import tc.oc.pgm.payload.events.CapturingTeamChangeEvent;
import tc.oc.pgm.payload.events.CapturingTimeChangeEvent;
import tc.oc.pgm.payload.events.ControllerChangeEvent;
import tc.oc.pgm.score.ScoreMatchModule;
import tc.oc.pgm.teams.TeamMatchModule;
import tc.oc.pgm.utils.Strings;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;

public class Payload extends OwnedGoal<PayloadDefinition> {

    public static final ChatColor COLOR_NEUTRAL_TEAM = ChatColor.WHITE;

    public static final String SYMBOL_CP_INCOMPLETE = "\u29be";     // ⦾
    public static final String SYMBOL_CP_COMPLETE = "\u279f";       // ➟

    protected final PayloadPlayerTracker playerTracker;

    private Location payloadLocation;

    private int railSize = 0;

    protected Minecart payloadEntity;
    protected ArmorStand labelEntity;

    private Path headPath;
    private Path tailPath;

    private Path currentPath;

    public List<Path> allCheckpoints = new LinkedList<>();
    public Set<Path> friendlyReachedCheckpoints = new HashSet<>();
    public Set<Path> enemyReachedCheckpoints = new HashSet<>();

    // This is set false after the first state change if definition.permanent == true
    protected boolean capturable = true;

    // The team that currently owns the point. The goal is completed for this team.
    // If this is null then the point is unowned, either because it is in the
    // neutral state, or because it has no initial currentOwner and has not yet been captured.
    protected Competitor currentOwner = null;

    // The team that will own the CP if the current capture is successful.
    // If this is null then either the point is not being captured or it is
    // being "uncaptured" toward the neutral state.
    protected Competitor capturer = null;

    // Time accumulated towards the next currentOwner change. When this passes timeToCapture,
    // it is reset to zero and the currentOwner changes to the capturer (which may be null,
    // if changing to the neutral state). When this is zero, the capturer is null.
    protected Duration progress = Duration.ZERO;

    public Payload(Match match, PayloadDefinition definition) {
        super(definition, match);

        if(this.definition.getInitialOwner() != null) {
            this.currentOwner = match.needMatchModule(TeamMatchModule.class).team(this.definition.getInitialOwner());
        }

        this.createPayload();

        this.playerTracker = new PayloadPlayerTracker(match, this.payloadLocation, this.definition.getRadius(), this.definition.getHeight(), this.payloadEntity);
    }

    public void registerEvents() {
        this.match.registerEvents(this.playerTracker);
    }

    public void unregisterEvents() {
        HandlerList.unregisterAll(this.playerTracker);
    }

    public PayloadPlayerTracker getPlayerTracker() {
        return playerTracker;
    }

    public Vector getStartingLocation() {
        return definition.getStartingLocation();
    }

    public Vector getSpawnLocation() {
        return definition.getSpawnLocation();
    }

    public float getYaw() {
        return definition.getYaw();
    }

    public Duration getTimeToCapture() {
        return definition.getTimeToCapture();
    }


    /**
     * The team that owns (is receiving points from) this Payload,
     * or null if the Payload is unowned.
     */
    public Competitor getCurrentOwner() {
        return this.currentOwner;
    }

    /**
     * The team that is "capturing" the Payload. This is the team
     * that the current capturingTime counts towards. The capturingTime
     * goes up whenever this team has the most players on the point,
     * and goes down when any other team has the most players on the point.
     * If capturingTime reaches timeToCapture, this team will take
     * ownership of the point, if they don't own it already. When capturingTime
     * goes below zero, the capturingTeam changes to the team with the most
     * players on the point, and the point becomes unowned.
     */
    public Competitor getCapturer() {
        return this.capturer;
    }

    /**
     * The partial currentOwner of the Payload. The "partial currentOwner" is defined in
     * three scenarios. If the Payload is owned and has a neutral state, the
     * partial currentOwner is the currentOwner of the Payload. If the Payload is in
     * contest, the partial currentOwner is the team that is currently capturing the
     * Payload. Lastly, if the Payload is un-owned and not in contest,
     * the progressingTeam is null.
     *
     * @return The team that should be displayed as having partial ownership of
     *         the point, if any.
     */
    public Competitor getPartialOwner() {
        return this.definition.hasNeutralState() && this.getCurrentOwner() != null ? this.getCurrentOwner() : this.getCapturer();
    }

    /**
     * Progress towards "capturing" the Payload for the current capturingTeam
     */
    public Duration getProgress() {
        return this.progress;
    }

    /**
     * Progress toward "capturing" the Payload for the current capturingTeam,
     * as a real number from 0 to 1.
     */
    public double getCompletion() {
        return (double) this.progress.toMillis() / (double) this.definition.getTimeToCapture().toMillis();
    }

    public String renderCompletion() {
        return Strings.progressPercentage(this.getCompletion());
    }

    public @Nullable String renderPreciseCompletion() {
        return null;
    }

    @Override
    public ChatColor renderSidebarStatusColor(@Nullable Competitor competitor, Party viewer) {
        return this.capturer == null ? COLOR_NEUTRAL_TEAM : this.capturer.getColor();

    }

    @Override
    public String renderSidebarStatusText(@Nullable Competitor competitor, Party viewer) {
        if(Duration.ZERO.equals(this.progress)) {
            return this.currentOwner == null ? SYMBOL_CP_INCOMPLETE : SYMBOL_CP_COMPLETE;
        } else {
            return this.renderCompletion();
        }
    }

    @Override
    public ChatColor renderSidebarLabelColor(@Nullable Competitor competitor, Party viewer) {
        return this.currentOwner == null ? COLOR_NEUTRAL_TEAM : this.currentOwner.getColor();
    }

    /**
     * Ownership of the Payload for a specific team given as a real number from
     * 0 to 1.
     */
    public double getCompletion(Competitor team) {
        if (this.getCurrentOwner() == team) {
            return 1 - this.getCompletion();
        } else if (this.getCapturer() == team) {
            return this.getCompletion();
        } else {
            return 0;
        }
    }

    public boolean getShowProgress() {
        return this.definition.getShowProgress();
    }

    @Override
    public boolean canComplete(Competitor team) {
        return this.canCapture(team);
    }

    @Override
    public boolean isCompleted() {
        return this.currentOwner != null;
    }

    @Override
    public boolean isCompleted(Competitor team) {
        return this.currentOwner != null && this.currentOwner == team;
    }

    private boolean canCapture(Competitor team) {
        return this.definition.getCaptureFilter() == null ||
               this.definition.getCaptureFilter().query(team).isAllowed();
    }

    private boolean canDominate(MatchPlayer player) {
        return this.definition.getPlayerFilter() == null ||
               this.definition.getPlayerFilter().query(player).isAllowed();
    }

    private Duration calculateDominateTime(int lead, Duration duration) {
        // Don't scale time if only one player is present, don't zero duration if multiplier is zero
        return TimeUtils.multiply(duration, 1 + (lead - 1) * definition.getTimeMultiplier());
    }

    public int getCheckpointCount() {
        return this.enemyReachedCheckpoints.size();
    }

    public void tick(Duration duration) {
        this.tickCapture(duration);
        this.tickDisplay();
        this.tickMove();
    }

    private void tickMove() {
        if (!this.capturable) {
            return;
        }

        if (this.currentOwner == null) {
            return;
        }

        double speed = isInEnemyControl() ? this.definition.getEnemySpeed() : this.definition.getFriendlySpeed();
        if (!isInEnemyControl() && this.currentPath.hasNext() && this.currentPath.next().isCheckpoint() && !this.definition.hasFriendlyCheckpoints()) {
            return;
        }

        Path finalPath = isInEnemyControl() ? this.tailPath : this.headPath;
        Location finalLocation = finalPath.getLocation();
        float points = isInEnemyControl() ? this.getDefinition().getPoints() : this.getDefinition().getFriendlyPoints();

        if (this.payloadLocation.getX() == finalLocation.getX() &&
                this.payloadLocation.getZ() == finalLocation.getZ()) {
            if (points > 0) {
                this.capturable = false;
                this.match.callEvent(new GoalCompleteEvent(this, this.currentOwner != null, c -> false, c -> c.equals(this.currentOwner)));

                ScoreMatchModule smm = this.getMatch().getMatchModule(ScoreMatchModule.class);
                if (smm != null) {
                    if (this.currentOwner != null) smm.incrementScore(this.currentOwner, points);
                }
                return;
            }
        }

        speed = Math.abs(speed);
        move(speed/10.0);

        this.payloadEntity.teleport(payloadLocation);
        this.playerTracker.setLocation(payloadLocation);

        Location labelLocation = this.payloadEntity.getLocation().clone();
        labelLocation.setY(labelLocation.getY() - 0.2);
        this.labelEntity.teleport(labelLocation);
    }

    private boolean isInEnemyControl() {
        return !super.getOwner().equals(this.getCurrentOwner());
    }

    private void move(double distance) {
        boolean hasNext = isInEnemyControl() ? currentPath.hasNext() : currentPath.hasPrevious();
        if (!hasNext) { // Path is over
            this.payloadLocation.setPosition(currentPath.getLocation().position());
            return;
        }

        if (currentPath.isCheckpoint()) {
            if (this.definition.hasFriendlyCheckpoints()) {
                if (isInEnemyControl() && this.enemyReachedCheckpoints.add(currentPath)) {
                    friendlyReachedCheckpoints.remove(currentPath);
                    final Component message = new Component(ChatColor.GRAY);
                    message.translate("match.payload.checkpoint",
                            this.getCurrentOwner().getComponentName());
                    match.sendMessage(message);
                } else if (!isInEnemyControl() && this.friendlyReachedCheckpoints.add(currentPath)) {
                    enemyReachedCheckpoints.remove(currentPath);
                    final Component message = new Component(ChatColor.GRAY);
                    message.translate("match.payload.checkpoint",
                            this.getCurrentOwner().getComponentName());
                    match.sendMessage(message);
                }
            } else if (isInEnemyControl() && this.enemyReachedCheckpoints.add(currentPath)) {
                final Component message = new Component(ChatColor.GRAY);
                message.translate("match.payload.checkpoint",
                        this.getCurrentOwner().getComponentName());
                match.sendMessage(message);
            }
        }

        Path nextPath = isInEnemyControl() ? currentPath.next() : currentPath.previous();
        Vector direction = nextPath.getLocation().position().minus(payloadLocation.position()).mutableCopy();
        double len = direction.length(),
                extraLen = distance - len;
        // If there's extra distance, skip calculations, otherwise, move payload proportionally
        if (extraLen > 0) {
            this.currentPath = nextPath;
            move(extraLen);
        } else this.payloadLocation.position().add(direction.multiply(distance / len));
    }

    private static final double TAU = Math.PI * 2;

    private int PARTICLE_BASE_COUNT = (int) Math.max(this.definition.getRadius() * Math.PI, 5);
    private int PARTICLE_SUB_COUNT = 3;
    private double ANGLE_PER_STEP = TAU / PARTICLE_BASE_COUNT;
    private double ANGLE_PER_SUB_STEP = 0.75 * ANGLE_PER_STEP / PARTICLE_SUB_COUNT;
    private double MIN_HEIGHT_OFFSET = 0.5; // Starting height of the bottom most particle
    private double HEIGHT_OFFSET = 0.75; // Vertical offset of each particle

    private void tickDisplay() {
        Color controllingColor = currentOwner != null ? currentOwner.getFullColor() : BukkitUtils.colorOf(COLOR_NEUTRAL_TEAM);
        Color capturingColor = capturer != null ? capturer.getFullColor() : BukkitUtils.colorOf(COLOR_NEUTRAL_TEAM);

        double completionAngle = (1 - getCompletion()) * TAU;

        for(int i = 0; i < PARTICLE_BASE_COUNT; i++) {
            double angleBasePoint = i * ANGLE_PER_STEP;
            for (int j = 0; j < PARTICLE_SUB_COUNT; j++) {
                double angle = angleBasePoint + ANGLE_PER_SUB_STEP * j;
                Color fullColor = (angle <= completionAngle) ? controllingColor : capturingColor;
                Location base = this.payloadLocation.clone().add(new Vector(
                        this.definition.getRadius() * Math.cos(angle),
                        j * HEIGHT_OFFSET + MIN_HEIGHT_OFFSET,
                        this.definition.getRadius() * Math.sin(angle)));
                match.getWorld().spawnParticle(
                        Particle.REDSTONE,
                        base,
                        0,
                        rgbToParticle(fullColor.getRed()),
                        rgbToParticle(fullColor.getGreen()),
                        rgbToParticle(fullColor.getBlue()),
                        1
                );
            }
        }
    }

    private double rgbToParticle(int rgb) {
        return Math.max(0.001, rgb / 255.0);
    }

    /**
     * Do a capturing cycle on this Payload over the given duration.
     */
    protected void tickCapture(Duration duration) {
        Map<Competitor, Integer> playerCounts = new DefaultMapAdapter<>(new HashMap<>(), 0);

        // The teams with the most and second-most capturing players on the point, respectively
        Competitor leader = null, runnerUp = null;

        // The total number of players on the point who are allowed to dominate and not on the leading team
        int defenderCount = 0;

        List<MatchPlayer> removePlayers = new ArrayList<>();
        for (MatchPlayer player : this.playerTracker.getPlayersOnPoint()) {
            if (!this.playerTracker.isOnPoint(player, player.getLocation().toVector())) {
                removePlayers.add(player);
                continue;
            }

            Competitor team = player.getCompetitor();
            if(this.canDominate(player)) {
                defenderCount++;
                int playerCount = playerCounts.get(team) + 1;
                playerCounts.put(team, playerCount);

                if(team != leader) {
                    if(leader == null || playerCount > playerCounts.get(leader)) {
                        runnerUp = leader;
                        leader = team;
                    } else if(team != runnerUp && (runnerUp == null || playerCount > playerCounts.get(runnerUp))) {
                        runnerUp = team;
                    }
                }
            }
        }

        for (MatchPlayer player : removePlayers) {
            this.playerTracker.removePlayerOnPoint(player);
        }

        int lead = 0;
        if(leader != null) {
            lead = playerCounts.get(leader);
            defenderCount -= lead;

            switch(this.definition.getCaptureCondition()) {
                case EXCLUSIVE:
                    if(defenderCount > 0) {
                        lead = 0;
                    }
                    break;

                case MAJORITY:
                    lead = Math.max(0, lead - defenderCount);
                    break;

                case LEAD:
                    if(runnerUp != null) {
                        lead -= playerCounts.get(runnerUp);
                    }
                    break;
            }
        }

        if(lead > 0) {
            this.dominateAndFireEvents(leader, calculateDominateTime(lead, duration));
        } else {
            this.dominateAndFireEvents(null, duration);
        }

    }

    /**
     * Do a cycle of domination on this Payload for the given team over the given duration. The team can be null,
     * which means no team is dominating the point, which can cause the state to change in some configurations.
     */
    private void dominateAndFireEvents(@Nullable Competitor dominator, Duration duration) {
        final Duration oldProgress = progress;
        final Competitor oldCapturer = capturer;
        final Competitor oldOwner = currentOwner;

        dominate(dominator, duration);

        if(!Objects.equals(oldCapturer, capturer) || !oldProgress.equals(progress)) {
            match.callEvent(new CapturingTimeChangeEvent(match, this));
            match.callEvent(new GoalStatusChangeEvent(this));
        }

        if(!Objects.equals(oldCapturer, capturer)) {
            match.callEvent(new CapturingTeamChangeEvent(match, this, oldCapturer, capturer));
        }

        if(!Objects.equals(oldOwner, currentOwner)) {
            match.callEvent(new ControllerChangeEvent(match, this, oldOwner, currentOwner));
        }
    }

    /**
     * If there is a neutral state, then the point cannot be owned and captured
     * at the same time. This means that at least one of controllingTeam or capturingTeam
     * must be null at any particular time.
     *
     * If controllingTeam is non-null, the point is owned, and it must be "uncaptured"
     * before any other team can capture it. In this state, capturingTeam is null,
     * the controlling team will decrease capturingTimeMillis, and all other teams will
     * increase it.
     *
     * If controllingTeam is null, then the point is in the neutral state. If capturingTeam
     * is also null, then the point is not being captured, and capturingTimeMillis is
     * zero. If capturingTeam is non-null, then that is the only team that will increase
     * capturingTimeMillis. All other teams will decrease it.
     *
     * If there is no neutral state, then the point is always either being captured
     * by a specific team, or not being captured at all.
     *
     * If incremental capturing is disabled, then capturingTimeMillis is reset to
     * zero whenever it stops increasing.
     */
    private void dominate(@Nullable Competitor dominator, Duration duration) {
        if(!capturable || Comparables.lessOrEqual(duration, Duration.ZERO)) {
            return;
        }

        if(currentOwner != null && definition.hasNeutralState()) {
            // Point is owned and has a neutral state
            if(Objects.equals(dominator, currentOwner)) {
                // Owner is recovering the point
                recover(duration, dominator);
            } else if(dominator != null) {
                // Non-currentOwner is uncapturing the point
                uncapture(duration, dominator);
            } else if (!this.playerTracker.hasPlayersOnPoint(currentOwner) && this.definition.emptyDecayRate() > 0) {
                //Nobody is on point and empty decay is enabled
                emptyDecay(duration);
            } else {
                // Point is decaying towards the currentOwner
                decay(duration);
            }
        } else if(capturer != null) {
            // Point is partly captured by someone
            if(Objects.equals(dominator, capturer)) {
                // Capturer is making progress
                capture(duration);
            } else if(dominator != null) {
                // Non-capturer is reversing progress
                recover(duration, dominator);
            } else {
                // Point is decaying towards currentOwner or neutral
                decay(duration);
            }
        } else if(dominator != null && !Objects.equals(dominator, currentOwner) && canCapture(dominator)) {
            // Point is not being captured and there is a dominant team that is not the currentOwner, so they start capturing
            capturer = dominator;
            dominate(dominator, duration);
        }
    }

    private @Nullable Duration addCaptureTime(final Duration duration) {
        progress = progress.plus(duration);
        if(Comparables.lessThan(progress, definition.getTimeToCapture())) {
            return null;
        } else {
            final Duration remainder = progress.minus(definition.getTimeToCapture());
            progress = Duration.ZERO;
            return remainder;
        }
    }

    private @Nullable Duration subtractCaptureTime(final Duration duration) {
        if(Comparables.greaterThan(progress, duration)) {
            progress = progress.minus(duration);
            return null;
        } else {
            final Duration remainder = duration.minus(progress);
            progress = Duration.ZERO;
            return remainder;
        }
    }

    /**
     * Point is owned, and a non-currentOwner is pushing it towards neutral
     */
    private void uncapture(Duration duration, Competitor dominator) {
        duration = addCaptureTime(duration);
        if(duration != null) {
            // If uncapture is complete, recurse with the dominant team's remaining time
            currentOwner = null;
            dominate(dominator, duration);

            byte blockData = BukkitUtils.chatColorToDyeColor(COLOR_NEUTRAL_TEAM).getWoolData();
            MaterialData payloadBlock = this.payloadEntity.getDisplayBlock();
            payloadBlock.setData(blockData);
            this.payloadEntity.setDisplayBlock(payloadBlock);
            this.labelEntity.setCustomName(this.getColoredName());
        }
    }
    /**
     * Point is owned, and a non-currentOwner is pushing it towards neutral
     */
    private void emptyDecay(Duration duration) {
        duration = TimeUtils.multiply(duration, 1.0/definition.emptyDecayRate());
        duration = addCaptureTime(duration);
        if(duration != null) {
            // If uncapture is complete, recurse with the dominant team's remaining time
            currentOwner = null;

            byte blockData = BukkitUtils.chatColorToDyeColor(COLOR_NEUTRAL_TEAM).getWoolData();
            MaterialData payloadBlock = this.payloadEntity.getDisplayBlock();
            payloadBlock.setData(blockData);
            this.payloadEntity.setDisplayBlock(payloadBlock);
            this.labelEntity.setCustomName(this.getColoredName());
        }
    }


    /**
     * Point is either owned or neutral, and someone is pushing it towards themselves
     */
    private void capture(Duration duration) {
        duration = addCaptureTime(duration);
        if(duration != null) {
            currentOwner = capturer;
            capturer = null;

            byte blockData = BukkitUtils.chatColorToDyeColor(currentOwner.getColor()).getWoolData();
            MaterialData payloadBlock = this.payloadEntity.getDisplayBlock();
            payloadBlock.setData(blockData);
            this.payloadEntity.setDisplayBlock(payloadBlock);
            this.labelEntity.setCustomName(this.getColoredName());
        }
    }

    /**
     * Point is being pulled back towards its current state
     */
    private void recover(Duration duration, Competitor dominator) {
        duration = TimeUtils.multiply(duration, definition.recoveryRate());
        duration = subtractCaptureTime(duration);
        if(duration != null) {
            capturer = null;
            if(!Objects.equals(dominator, currentOwner)) {
                // If the dominant team is not the controller, recurse with the remaining time
                dominate(dominator, TimeUtils.multiply(duration, 1D / definition.recoveryRate()));
            }
        }
    }

    /**
     * Point is decaying back towards its current state
     */
    private void decay(Duration duration) {
        duration = TimeUtils.multiply(duration, definition.decayRate());
        duration = subtractCaptureTime(duration);
        if(duration != null) {
            capturer = null;
        }
    }

    protected void createPayload() {
        this.makePath();
        this.summonMinecart();
    }

    protected void summonMinecart() {
        Location location = this.getSpawnLocation().toLocation(getMatch().getWorld());

        this.payloadLocation = location;

        //Set the floor to gold
        Location below = location.clone();
        below.setY(location.getY() - 1);
        below.getBlock().setType(Material.GOLD_BLOCK);

        //Spawn the Payload entity
        Location spawn = location.clone();
        spawn.setYaw(this.getYaw());
        this.payloadEntity = location.getWorld().spawn(spawn, Minecart.class);
        ChatColor color = currentOwner != null ? currentOwner.getColor() : COLOR_NEUTRAL_TEAM;
        byte blockData = BukkitUtils.chatColorToDyeColor(color).getWoolData();
        MaterialData payloadBlock = new MaterialData(Material.STAINED_CLAY);
        payloadBlock.setData(blockData);
        this.payloadEntity.setDisplayBlock(payloadBlock);
        this.payloadEntity.setInvulnerable(true);
        this.payloadEntity.setGravity(false);
        this.payloadEntity.setMaxSpeed(0);
        this.payloadEntity.setSlowWhenEmpty(true);

        //Summon a label for it
        this.labelEntity = this.payloadLocation.getWorld().spawn(this.payloadLocation.clone().add(0, 0.2, 0), ArmorStand.class);
        this.labelEntity.setVisible(false);
        this.labelEntity.setGravity(false);
        this.labelEntity.setRemoveWhenFarAway(false);
        this.labelEntity.setArms(false);
        this.labelEntity.setBasePlate(false);
        this.labelEntity.setCustomName(this.getColoredName());
        this.labelEntity.setCustomNameVisible(true);
        this.labelEntity.setInvulnerable(true);
        this.labelEntity.setMarker(true);
        NMSHacks.enableArmorSlots(this.labelEntity, false);
    }

    class Path {
        private int index;
        private Location location;
        private Path previousPath;
        private Path nextPath;
        private boolean checkpoint;

        Path(Location location, Path previousPath, Path nextPath) {
            this(0, location, previousPath, nextPath, false);
        }

        Path(int index, Location location, Path previousPath, Path nextPath) {
            this(index, location, previousPath, nextPath, false);
        }

        Path(Location location, Path previousPath, Path nextPath, boolean checkpoint) {
            this(0, location, previousPath, nextPath, checkpoint);
        }

        Path(int index, Location location, Path previousPath, Path nextPath, boolean checkpoint) {
            this.index = index;
            this.location = location;
            this.previousPath = previousPath;
            this.nextPath = nextPath;
            this.checkpoint = checkpoint;
        }

        public int getIndex() {
            return index;
        }

        public Location getLocation() {
            return location;
        }

        public boolean hasPrevious() {
            return previous() != null;
        }

        public Path previous() {
            return previousPath;
        }

        public void setPrevious(Path previousPath) {
            this.previousPath = previousPath;
        }

        public boolean hasNext() {
            return next() != null;
        }

        public Path next() {
            return nextPath;
        }

        public void setNext(Path nextPath) {
            this.nextPath = nextPath;
        }

        public boolean isCheckpoint() {
            return checkpoint;
        }
    }

    protected void makePath() {
        Location location = this.getStartingLocation().toLocation(getMatch().getWorld());

        //Payload must start on a rail
        if (!isRails(location.getBlock().getType())) {
            return;
        }

        Rails startingRails = (Rails) location.getBlock().getState().getMaterialData();

        if (startingRails.isCurve() || startingRails.isOnSlope()) {
            return;
        }

        BlockFace direction = startingRails.getDirection();

        List<Double> differingX = new ArrayList<>();
        List<Double> differingY = new ArrayList<>();
        List<Double> differingZ = new ArrayList<>();

        differingY.add(0.0);
        differingY.add(1.0);
        differingY.add(-1.0);

        headPath = new Path(this.railSize, location, null, null);
        this.railSize++;

        Path previousPath = headPath;
        Path neighborRail = getNewNeighborRail(previousPath, direction, differingX, differingY, differingZ);

        while (neighborRail != null) {
            previousPath.setNext(neighborRail);

            previousPath = neighborRail;

            differingX.clear();
            differingZ.clear();

            if (previousPath.getLocation().getBlock().getState().getMaterialData() instanceof Rails) {
                direction = ((Rails)previousPath.getLocation().getBlock().getState().getMaterialData()).getDirection();
            } else {
                direction = null;
            }

            neighborRail = getNewNeighborRail(previousPath, direction, differingX, differingY, differingZ);
        }

        tailPath = previousPath;

        Path currentPath = headPath;
        Path lastPath = null;

        headPath = null;

        boolean reachedMiddle = false;
        boolean moreRails = currentPath.hasNext();
        while (moreRails) {
            Path nextPath = currentPath.next();
            Location newLocation = currentPath.getLocation().toVector().midpoint(nextPath.getLocation().toVector()).toLocation(getMatch().getWorld());
            newLocation.setY(Math.max(currentPath.getLocation().getY(), nextPath.getLocation().getY()));
            Path newPath;
            if (headPath == null) {
                Location headLocation = newLocation.clone().add(currentPath.getLocation().subtract(nextPath.getLocation()));
                headPath = new Path(this.railSize, headLocation, null, null);
                this.railSize++;
                newPath = new Path(this.railSize, newLocation, headPath, null);
                this.railSize++;
                headPath.setNext(newPath);
                lastPath = newPath;
                this.currentPath = headPath;
            } else {
                newPath = new Path(this.railSize, newLocation, lastPath, null, currentPath.isCheckpoint());

                this.railSize++;
                lastPath.setNext(newPath);
                lastPath = newPath;
                tailPath = lastPath;
            }

            if (this.getSpawnLocation().getX() == currentPath.getLocation().getX() &&
                    this.getSpawnLocation().getY() == currentPath.getLocation().getY() &&
                    this.getSpawnLocation().getZ() == currentPath.getLocation().getZ()) {
                reachedMiddle = true;
                this.currentPath = newPath;
                if (currentPath.isCheckpoint()) {
                    this.allCheckpoints.add(lastPath);
                }
            } else if (currentPath.isCheckpoint()) {
                this.allCheckpoints.add(lastPath);
                boolean add = reachedMiddle ? this.friendlyReachedCheckpoints.add(lastPath) : this.enemyReachedCheckpoints.add(lastPath);
            }
            currentPath = nextPath;
            moreRails = currentPath.hasNext();
        }

        Path tail = tailPath;
        Path beforeTail = tail.previous();
        Location newLocation = tail.getLocation().getLocation().toVector().midpoint(beforeTail.getLocation().toVector()).toLocation(getMatch().getWorld());
        newLocation.setY(Math.max(tail.getLocation().getY(), beforeTail.getLocation().getY()));
        Location tailLocation = newLocation.clone().add(currentPath.getLocation().subtract(beforeTail.getLocation()));
        tailPath = new Path(tailLocation, tail, null);
        tail.setNext(tailPath);
    }

    public boolean isRails(Material material) {
        return material.equals(Material.RAILS);
    }

    public boolean isCheckpoint(Material material) {
        return this.definition.getCheckpointMaterial() != null ?
                material.equals(this.definition.getCheckpointMaterial().getMaterial()) :
                material.equals(Material.ACTIVATOR_RAIL) ||
                material.equals(Material.DETECTOR_RAIL) ||
                material.equals(Material.POWERED_RAIL);
    }

    public Path getNewNeighborRail(Path path, BlockFace direction, List<Double> differingX, List<Double> differingY, List<Double> differingZ) {
        Location previousLocation = null;
        if (path.previous() != null) {
            previousLocation = path.previous().getLocation();
        }

        Location location = path.getLocation();

        if (direction == null) {
            differingX.add(-1.0);
            differingX.add(0.0);
            differingX.add(1.0);
            differingZ.add(-1.0);
            differingZ.add(0.0);
            differingZ.add(1.0);
        } else if (direction.equals(BlockFace.SOUTH) || direction.equals(BlockFace.NORTH)) {
            differingZ.add(-1.0);
            differingZ.add(1.0);
            differingX.add(0.0);
        } else if (direction.equals(BlockFace.EAST) || direction.equals(BlockFace.WEST)) {
            differingX.add(-1.0);
            differingX.add(1.0);
            differingZ.add(0.0);
        } else {
            Location side = location.clone();
            side.setZ(side.getZ() + (direction.equals(BlockFace.NORTH_WEST) || direction.equals(BlockFace.NORTH_EAST) ? 1 : -1));
            if (side.getX() == previousLocation.getX() && side.getZ() == previousLocation.getZ()) {
                differingX.add(direction.equals(BlockFace.SOUTH_WEST) || direction.equals(BlockFace.NORTH_WEST) ? 1.0 : -1.0);
                differingZ.add(0.0);
            } else {
                differingX.add(0.0);
                differingZ.add(direction.equals(BlockFace.NORTH_WEST) || direction.equals(BlockFace.NORTH_EAST) ? 1.0 : -1.0);
            }
        }

        Location newLocation = location.clone();
        for (double x : differingX) {
            for (double y : differingY) {
                for (double z : differingZ) {
                    newLocation.add(x, y, z);

                    boolean isCheckpoint = isCheckpoint(newLocation.getBlock().getType());

                    if (isRails(newLocation.getBlock().getType()) || isCheckpoint) {
                        Path currentPath = path;

                        boolean alreadyExists = false;

                        if (headPath.getLocation().getX() == newLocation.getX() &&
                                headPath.getLocation().getY() == newLocation.getY() &&
                                headPath.getLocation().getZ() == newLocation.getZ()) {
                            alreadyExists = true;
                        }

                        while (currentPath.hasPrevious()) {
                            if (currentPath.getLocation().getX() == newLocation.getX() &&
                                    currentPath.getLocation().getY() == newLocation.getY() &&
                                    currentPath.getLocation().getZ() == newLocation.getZ()) {
                                alreadyExists = true;
                            }
                            currentPath = currentPath.previous();
                        }

                        if (!alreadyExists) {
                            return new Path(newLocation, path, null, isCheckpoint);
                        }
                    }

                    newLocation.subtract(x, y, z);
                }
            }
        }
        return null;
    }

    @Override
    public MatchDoc.OwnedGoal getDocument() {
        return new Document();
    }

    class Document extends SimpleGoal.Document implements MatchDoc.OwnedGoal {
        @Override
        public @Nullable String owner_id() {
            return getOwner() == null ? null : getOwner().slug();
        }

        @Override
        public @Nullable String owner_name() {
            return getOwner() == null ? null : getOwner().getName();
        }
    }
}
