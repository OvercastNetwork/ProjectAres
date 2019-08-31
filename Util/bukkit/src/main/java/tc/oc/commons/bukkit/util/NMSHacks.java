package tc.oc.commons.bukkit.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.Block;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.DataWatcher;
import net.minecraft.server.Enchantment;
import net.minecraft.server.EntityArmorStand;
import net.minecraft.server.EntityChicken;
import net.minecraft.server.EntityFireworks;
import net.minecraft.server.EntityItem;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntityTrackerEntry;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.EnumGamemode;
import net.minecraft.server.EnumHand;
import net.minecraft.server.EnumItemSlot;
import net.minecraft.server.EnumParticle;
import net.minecraft.server.IBlockData;
import net.minecraft.server.Item;
import net.minecraft.server.ItemStack;
import net.minecraft.server.Items;
import net.minecraft.server.MinecraftKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.MobEffect;
import net.minecraft.server.MobEffectList;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.Packet;
import net.minecraft.server.PacketPlayInUseEntity;
import net.minecraft.server.PacketPlayOutEntity;
import net.minecraft.server.PacketPlayOutEntityDestroy;
import net.minecraft.server.PacketPlayOutEntityEffect;
import net.minecraft.server.PacketPlayOutEntityEquipment;
import net.minecraft.server.PacketPlayOutEntityMetadata;
import net.minecraft.server.PacketPlayOutEntityTeleport;
import net.minecraft.server.PacketPlayOutEntityVelocity;
import net.minecraft.server.PacketPlayOutMount;
import net.minecraft.server.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.PacketPlayOutPlayerInfo;
import net.minecraft.server.PacketPlayOutRemoveEntityEffect;
import net.minecraft.server.PacketPlayOutScoreboardTeam;
import net.minecraft.server.PacketPlayOutSpawnEntity;
import net.minecraft.server.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.PacketPlayOutWorldBorder;
import net.minecraft.server.PacketPlayOutWorldParticles;
import net.minecraft.server.PlayerInteractManager;
import net.minecraft.server.SoundCategory;
import net.minecraft.server.SoundEffect;
import net.minecraft.server.TileEntitySkull;
import net.minecraft.server.WorldBorder;
import net.minecraft.server.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Skin;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftArmorStand;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftFirework;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.registry.CraftKey;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.Skins;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.bukkit.registry.Key;
import org.bukkit.util.Vector;
import java.time.Duration;
import tc.oc.commons.core.util.TimeUtils;

import static com.google.common.base.Preconditions.checkArgument;

public class NMSHacks {

    private static final Random random = new Random();

    // These entity type IDs are hard-coded in a huge conditional statement in EntityTrackerEntry.
    // There is no nice way to get at them.
    private static final Map<Class<? extends Entity>, Integer> ENTITY_TYPE_IDS = ImmutableMap.of(
        org.bukkit.entity.Item.class, 2,
        ArmorStand.class, 78
    );

    private static EntityTrackerEntry getTrackerEntry(net.minecraft.server.Entity nms) {
        return ((WorldServer) nms.getWorld()).getTracker().trackedEntities.get(nms.getId());
    }

    private static EntityTrackerEntry getTrackerEntry(Entity entity) {
        return getTrackerEntry(((CraftEntity) entity).getHandle());
    }

    public static void sendPacket(Player bukkitPlayer, Object packet) {
        if (bukkitPlayer.isOnline()) {
            EntityPlayer nmsPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
            nmsPlayer.playerConnection.sendPacket((Packet) packet);
        }
    }

    private static void sendPacketToViewers(Entity entity, Object packet) {
        EntityTrackerEntry entry = getTrackerEntry(entity);
        for(EntityPlayer viewer : entry.trackedPlayers) {
            viewer.playerConnection.sendPacket((Packet) packet);
        }
    }

    public static PacketPlayOutPlayerInfo.PlayerInfoData playerListPacketData(PacketPlayOutPlayerInfo packet, UUID uuid, String name, @Nullable BaseComponent displayName, GameMode gamemode, int ping, @Nullable Skin skin) {
        GameProfile profile = new GameProfile(uuid, name);
        if(skin != null) {
            for(Map.Entry<String, Collection<Property>> entry : Skins.toProperties(skin).asMap().entrySet()) {
                profile.getProperties().putAll(entry.getKey(), entry.getValue());
            }
        }
        PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(profile, ping, gamemode == null ? null : EnumGamemode.getById(gamemode.getValue()), null);
        data.displayName = displayName == null ? null : new BaseComponent[]{ displayName };
        return data;
    }

    public static PacketPlayOutPlayerInfo.PlayerInfoData playerListPacketData(PacketPlayOutPlayerInfo packet, UUID uuid, BaseComponent displayName) {
        return playerListPacketData(packet, uuid, null, displayName, null, 0, null);
    }

    public static PacketPlayOutPlayerInfo.PlayerInfoData playerListPacketData(PacketPlayOutPlayerInfo packet, UUID uuid) {
        return playerListPacketData(packet, uuid, null, null, null, 0, null);
    }

    public static Packet playerListAddPacket(UUID uuid, String name, @Nullable BaseComponent displayName, GameMode gamemode, int ping, @Nullable Skin skin) {
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
        packet.add(playerListPacketData(packet, uuid, name, displayName, gamemode, ping, skin));
        return packet;
    }

    public static Packet teamPacket(int operation,
                                    String name,
                                    String displayName,
                                    String prefix,
                                    String suffix,
                                    boolean friendlyFire,
                                    boolean seeFriendlyInvisibles,
                                    String nameTagVisibility,
                                    String collisionRule,
                                    Collection<String> players) {

        int flags = 0;
        if(friendlyFire) { flags |= 1; }
        if(seeFriendlyInvisibles) { flags |= 2; }

        return new PacketPlayOutScoreboardTeam(operation,
                                               name, displayName, prefix, suffix,
                                               0, // color
                                               nameTagVisibility,
                                               collisionRule,
                                               flags,
                                               players);
    }

    public static Packet teamCreatePacket(String name,
                                          String displayName,
                                          String prefix,
                                          String suffix,
                                          boolean friendlyFire,
                                          boolean seeFriendlyInvisibles,
                                          String nameTagVisibility,
                                          String collisionRule,
                                          Collection<String> players) {
        return teamPacket(0, name, displayName, prefix, suffix, friendlyFire, seeFriendlyInvisibles, nameTagVisibility, collisionRule, players);
    }

    public static Packet teamCreatePacket(String name,
                                          String displayName,
                                          String prefix,
                                          String suffix,
                                          boolean friendlyFire,
                                          boolean seeFriendlyInvisibles,
                                          Collection<String> players) {
        return teamCreatePacket(name, displayName, prefix, suffix, friendlyFire, seeFriendlyInvisibles, "always", "always", players);
    }

    public static Packet teamRemovePacket(String name) {
        return teamPacket(1, name, null, null, null, false, false, null, null, Lists.<String>newArrayList());
    }

    public static Packet teamJoinPacket(String name, Collection<String> players) {
        return teamPacket(3, name, null, null, null, false, false, null, null, players);
    }

    public static Packet teamLeavePacket(String name, Collection<String> players) {
        return teamPacket(4, name, null, null, null, false, false, null, null, players);
    }

    private static List<DataWatcher.Item<?>> copyEntityMetadata(Entity entity) {
        final List<DataWatcher.Item<?>> metadata = ((CraftEntity) entity).getHandle().getDataWatcher().c();
        DataWatcher.deepCopy(metadata);
        return metadata;
    }

    private static byte encodeAngle(float angle) {
        return (byte) (angle * 256f / 360f);
    }

    private static int encodeVelocity(double v) {
        return (int) (v * 8000D);
    }

    private static long encodePosition(double d) {
        return (long) (d * 4096D);
    }

    public static Packet destroyEntitiesPacket(int... entityIds) {
        return new PacketPlayOutEntityDestroy(entityIds);
    }

    public static Packet spawnEntityPacket(Class<? extends Entity> type, int data, int entityId, UUID uuid, Location location, Vector velocity) {
        checkArgument(ENTITY_TYPE_IDS.containsKey(type));
        return new PacketPlayOutSpawnEntity(entityId, uuid,
                                            location.getX(), location.getY(), location.getZ(),
                                            encodeVelocity(velocity.getX()), encodeVelocity(velocity.getY()), encodeVelocity(velocity.getZ()),
                                            encodeAngle(location.getPitch()), encodeAngle(location.getYaw()),
                                            ENTITY_TYPE_IDS.get(type), data);
    }

    public static Packet spawnPlayerPacket(int entityId, UUID uuid, Location location, Player player) {
        return new PacketPlayOutNamedEntitySpawn(entityId,
                                                 uuid,
                                                 location.getX(), location.getY(), location.getZ(),
                                                 encodeAngle(location.getYaw()),
                                                 encodeAngle(location.getPitch()),
                                                 copyEntityMetadata(player));
    }

    public static Packet spawnPlayerPacket(int entityId, UUID uuid, Location location, List<DataWatcher.Item<?>> metadata) {
        return new PacketPlayOutNamedEntitySpawn(entityId,
                                                 uuid,
                                                 location.getX(), location.getY(), location.getZ(),
                                                 encodeAngle(location.getYaw()),
                                                 encodeAngle(location.getPitch()),
                                                 metadata);
    }

    public static Packet setPassengerPacket(int vehicleId, int riderId) {
        return new PacketPlayOutMount(vehicleId, riderId);
    }

    public static Packet moveEntityRelativePacket(int entityId, Vector delta, boolean onGround) {
        return new PacketPlayOutEntity.PacketPlayOutRelEntityMove(entityId,
                                                                  encodePosition(delta.getX()),
                                                                  encodePosition(delta.getY()),
                                                                  encodePosition(delta.getZ()),
                                                                  onGround);
    }

    public static Packet teleportEntityPacket(int entityId, Location location) {
        return new PacketPlayOutEntityTeleport(entityId,
                                               location.getX(), location.getY(), location.getZ(),
                                               encodeAngle(location.getYaw()),
                                               encodeAngle(location.getPitch()),
                                               true);
    }

    private static Packet entityMetadataPacket(int entityId, net.minecraft.server.Entity nmsEntity, boolean complete) {
        return new PacketPlayOutEntityMetadata(entityId, nmsEntity.getDataWatcher(), complete);
    }

    public static Packet entityMetadataPacket(int entityId, Entity entity, boolean complete) {
        return entityMetadataPacket(entityId, ((CraftEntity) entity).getHandle(), complete);
    }

    public static Packet entityMetadataPacket(net.minecraft.server.Entity nmsEntity, boolean complete) {
        return entityMetadataPacket(nmsEntity.getId(), nmsEntity, complete);
    }

    public static Packet entityHelmetPacket(int entityId, org.bukkit.inventory.ItemStack helmet) {
        return new PacketPlayOutEntityEquipment(entityId, EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(helmet));
    }

    /**
     * Immediately send the given entity's metadata to all viewers in range
     */
    public static void sendEntityMetadataToViewers(Entity entity, boolean complete) {
        sendPacketToViewers(entity, entityMetadataPacket(entity.getEntityId(), entity, complete));
    }

    public interface FakeEntity {
        int entityId();

        void spawn(Player viewer, Location location, Vector velocity);

        default void spawn(Player viewer, Location location) {
            spawn(viewer, location, Vectors.ZERO);
        }

        default void destroy(Player viewer) {
            sendPacket(viewer, destroyEntitiesPacket(entityId()));
        }

        default void move(Player viewer, Vector delta, boolean onGround) {
            sendPacket(viewer, moveEntityRelativePacket(entityId(), delta, onGround));
        }

        default void teleport(Player viewer, Location location) {
            sendPacket(viewer, teleportEntityPacket(entityId(), location));
        }

        default void ride(Player viewer, Entity rider) {
            sendPacket(viewer, setPassengerPacket(entityId(), rider.getEntityId()));
        }
    }

    private static abstract class FakeEntityImpl<T extends net.minecraft.server.Entity> implements FakeEntity {
        protected final T entity;

        protected FakeEntityImpl(T entity) {
            this.entity = entity;
        }

        @Override
        public int entityId() {
            return entity.getId();
        }
    }

    public static class FakeArmorStand extends FakeEntityImpl<EntityArmorStand> {
        public FakeArmorStand(World world) {
            this(world, null);
        }

        public FakeArmorStand(World world, @Nullable String name) {
            this(world, name, true, true, true);
        }

        public FakeArmorStand(World world, @Nullable String name, boolean invisible, boolean marker, boolean small) {
            super(new EntityArmorStand(((CraftWorld) world).getHandle()));

            entity.setInvisible(invisible);
            entity.setMarker(marker);
            entity.setSmall(small);
            entity.setBasePlate(false);
            entity.setArms(false);

            if(name != null) {
                entity.setCustomName(name);
                entity.setCustomNameVisible(true);
            }
        }

        @Override
        public void spawn(Player viewer, Location location, Vector velocity) {
            sendPacket(viewer, spawnEntityPacket(ArmorStand.class, 0, entityId(), entity.getUniqueID(), location, velocity));
            sendPacket(viewer, entityMetadataPacket(entity, true));
        }
    }

    private static class FakeLivingEntity<T extends EntityLiving> extends FakeEntityImpl<T> {

        protected FakeLivingEntity(T entity) {
            super(entity);
        }

        @Override
        public void spawn(Player viewer, Location location, Vector velocity) {
            entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            entity.motX = velocity.getX();
            entity.motY = velocity.getY();
            entity.motZ = velocity.getZ();
            sendPacket(viewer, spawnPacket());
        }

        protected Packet<?> spawnPacket() {
            return new PacketPlayOutSpawnEntityLiving(entity);
        }
    }

    public static class FakeZombie extends FakeLivingEntity<EntityZombie> {
        public FakeZombie(World world, boolean invisible) {
            super(new EntityZombie(((CraftWorld) world).getHandle()));

            entity.setInvisible(invisible);
            entity.setNoAI(true);
        }
    }

    public static class FakeChicken extends FakeLivingEntity<EntityChicken> {
        public FakeChicken(World world, String name) {
            super(new EntityChicken(((CraftWorld) world).getHandle()));

            entity.setCustomName(name);
            entity.setCustomNameVisible(true);
        }

        @Override
        public void spawn(Player viewer, Location location, Vector velocity) {
            super.spawn(viewer, location, velocity);
            sendPacket(viewer, entityMetadataPacket(entity, true));
        }
    }

    public static class FakePlayer implements FakeEntity {

        private static @Nullable EntityPlayer prototype;
        private static EntityPlayer prototype() {
            if(prototype == null) {
                final MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
                final net.minecraft.server.WorldServer nmsWorld = nmsServer.worldServer[0];
                prototype = new EntityPlayer(nmsServer, nmsWorld, new GameProfile(UUID.randomUUID(), ""),
                                             new PlayerInteractManager(nmsWorld));
                prototype.setInvisible(true);
            }
            return prototype;
        }

        private final int entityId;
        private final UUID uuid;
        private @Nullable Packet destroyPacket;
        private @Nullable Location location;
        private @Nullable Packet teleportPacket;
        private @Nullable org.bukkit.inventory.ItemStack helmet;
        private @Nullable Packet helmetPacket;

        public FakePlayer() {
            this(Bukkit.allocateEntityId(), UUID.randomUUID());
        }

        public FakePlayer(int entityId, UUID uuid) {
            this.entityId = entityId;
            this.uuid = uuid;
        }

        @Override
        public int entityId() {
            return entityId;
        }

        @Override
        public void spawn(Player viewer, Location location, Vector velocity) {
            spawn(viewer, uuid, location);
            if(!velocity.isZero()) {
                sendPacket(viewer, new PacketPlayOutEntityVelocity(entityId(), velocity.getX(), velocity.getY(), velocity.getZ()));
            }
        }

        public void spawn(Player viewer, UUID uuid, Location location) {
            sendPacket(viewer, spawnPlayerPacket(entityId(),
                                                 uuid,
                                                 location,
                                                 prototype().getDataWatcher().c()));
        }

        private Packet destroyPacket() {
            if(destroyPacket == null) {
                destroyPacket = destroyEntitiesPacket(entityId());
            }
            return destroyPacket;
        }

        @Override
        public void destroy(Player viewer) {
            sendPacket(viewer, destroyPacket());
        }

        @Override
        public void teleport(Player viewer, Location location) {
            if(!Objects.equals(this.location, location)) {
                this.location = location.clone();
                teleportPacket = teleportEntityPacket(entityId(), location);
            }
            sendPacket(viewer, teleportPacket);
        }

        public void wearHelmet(Player viewer, org.bukkit.inventory.ItemStack helmet) {
            if(!Objects.equals(this.helmet, helmet)) {
                this.helmet = helmet.clone();
                helmetPacket = entityHelmetPacket(entityId(), helmet);
            }
            sendPacket(viewer, helmetPacket);
        }
    }

    private enum MetadataFlag {
        BURNING(0x01),
        SNEAKING(0x02),
        SPRINTING(0x08),
        EATING(0x10),
        INVISIBLE(0x20),
        GLOWING(0x40),
        GLIDING(0x80);

        final int mask;

        MetadataFlag(int mask) {
            this.mask = mask;
        }
    }

    private enum ArmorStandFlag {
        SMALL(0x01),
        GRAVITY(0x02),
        ARMS(0x04),
        BASEPLATE(0x08),
        MARKER(0x10);

        final int mask;

        ArmorStandFlag(int mask) {
            this.mask = mask;
        }
    }

    private static double randomEntityVelocity() {
        return random.nextDouble() - 0.5d;
    }

    public static void showFakeItems(Plugin plugin, Player viewer, Location location, org.bukkit.inventory.ItemStack item, int count, Duration duration) {
        if(count <= 0) return;

        final EntityPlayer nmsPlayer = ((CraftPlayer) viewer).getHandle();
        final int[] entityIds = new int[count];

        for(int i = 0; i < count; i++) {
            final EntityItem entity = new EntityItem(nmsPlayer.getWorld(), location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(item));

            entity.motX = randomEntityVelocity();
            entity.motY = randomEntityVelocity();
            entity.motZ = randomEntityVelocity();

            sendPacket(viewer, new PacketPlayOutSpawnEntity(entity, ENTITY_TYPE_IDS.get(org.bukkit.entity.Item.class)));
            sendPacket(viewer, new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true));

            entityIds[i] = entity.getId();
        }

        scheduleEntityDestroy(plugin, viewer.getUniqueId(), duration, entityIds);
    }

    private static void scheduleEntityDestroy(Plugin plugin, UUID viewerUuid, Duration delay, int[] entityIds) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            final Player viewer = plugin.getServer().getPlayer(viewerUuid);
            if(viewer != null) {
                sendPacket(viewer, new PacketPlayOutEntityDestroy(entityIds));
            }
        }, TimeUtils.toTicks(delay));
    }

    public static void setFireworksExpectedLifespan(Firework firework, int ticks) {
        ((CraftFirework) firework).getHandle().expectedLifespan = ticks;
    }

    public static void setFireworksTicksFlown(Firework firework, int ticks) {
        EntityFireworks entityFirework = ((CraftFirework) firework).getHandle();
        entityFirework.ticksFlown = ticks;
    }

    public static void skipFireworksLaunch(Firework firework) {
        setFireworksExpectedLifespan(firework, 2);
        setFireworksTicksFlown(firework, 2);
        sendEntityMetadataToViewers(firework, false);
    }

    public static void playEffect(World bukkitWorld, Vector pos, int effectId, int data) {
        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        world.triggerEffect(effectId, new BlockPosition(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()), data);
    }

    @SuppressWarnings("deprecation")
    public static void playBlockBreakEffect(World bukkitWorld, Vector pos, Material material) {
        playEffect(bukkitWorld, pos, 2001, material.getId());
    }

    private static void playCustomSound(World bukkitWorld, Location location, SoundEffect sound, SoundCategory category, Float volume, Float pitch) {
        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        world.playSoundEffect(null, location.getX(), location.getY(), location.getZ(), sound, category, volume, pitch);
    }

    public static void playBlockPlaceSound(World bukkitWorld, Vector pos, Material material, float volume) {
        if (!material.isBlock()) {
            return;
        }
        playCustomSound(bukkitWorld,
                        pos.toLocation(bukkitWorld),
                        CraftMagicNumbers.getBlock(material).getStepSound().placeSound(),
                        SoundCategory.BLOCKS,
                        volume,
                        1f);
    }

    /**
     * Test if the given tool is capable of "efficiently" mining the given block.
     *
     * Derived from CraftBlock.itemCausesDrops()
     */
    public static boolean canMineBlock(MaterialData blockMaterial, org.bukkit.inventory.ItemStack tool) {
        if(!blockMaterial.getItemType().isBlock()) {
            throw new IllegalArgumentException("Material '" + blockMaterial + "' is not a block");
        }

        net.minecraft.server.Block nmsBlock = CraftMagicNumbers.getBlock(blockMaterial.getItemType());
        net.minecraft.server.Item nmsTool = tool == null ? null : CraftMagicNumbers.getItem(tool.getType());

        return nmsBlock != null && (nmsBlock.getBlockData().getMaterial().isAlwaysDestroyable() ||
                                    (nmsTool != null && nmsTool.canDestroySpecialBlock(nmsBlock.getBlockData())));
    }

    public static long getMonotonicTime(World world) {
        return ((CraftWorld) world).getHandle().getTime();
    }

    public static void createExplosion(Entity entity, Location loc, float power, boolean fire, boolean destroy) {
        ((CraftWorld) loc.getWorld()).getHandle().createExplosion(((CraftEntity) entity).getHandle(), loc.getX(), loc.getY(), loc.getZ(), power, fire, destroy);
    }

    /**
     * Test if a {@link Skull} has a cached skin. If this returns false, the skull will
     * likely try to fetch its skin the next time it is loaded.
     */
    public static boolean isSkullCached(Skull skull) {
        TileEntitySkull nmsSkull = (TileEntitySkull) ((CraftWorld) skull.getWorld()).getTileEntityAt(skull.getX(), skull.getY(), skull.getZ());
        return nmsSkull.getGameProfile() == null ||
               nmsSkull.getGameProfile().getProperties().containsKey("textures");
    }

    public static void enableArmorSlots(ArmorStand armorStand, boolean enabled) {
        CraftArmorStand craftArmorStand = (CraftArmorStand) armorStand;
        NBTTagCompound nbt = new NBTTagCompound();
        craftArmorStand.getHandle().b(nbt);
        nbt.setInt("DisabledSlots", enabled ? 0 : 0x1f1f00);
        craftArmorStand.getHandle().a(nbt);
    }

    public static Object particlesPacket(String name, boolean longRange, Vector pos, Vector offset, float data, int count, int... extra) {
        return new PacketPlayOutWorldParticles(EnumParticle.valueOf(EnumParticle.class, name),
                                               longRange,
                                               (float) pos.getX(), (float) pos.getY(), (float) pos.getZ(),
                                               (float) offset.getX(), (float) offset.getY(), (float) offset.getZ(),
                                               data,
                                               count,
                                               extra);
    }

    public static Object blockCrackParticlesPacket(MaterialData material, boolean longRange, Vector pos, Vector offset, float data, int count) {
        return particlesPacket("BLOCK_CRACK", longRange, pos, offset, data, count, material.getItemTypeId() + (material.getData() << 12));
    }

    public static void showBorderWarning(Player player, boolean show) {
        WorldBorder border = new WorldBorder();
        border.setWarningDistance(show ? Integer.MAX_VALUE : 0);
        sendPacket(player, new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS));
    }

    public static void playDeathAnimation(Player player) {
        EntityPlayer handle = ((CraftPlayer) player).getHandle();
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(handle.getId(), handle.getDataWatcher(), false);

        // Add/replace health to zero
        boolean replaced = false;
        DataWatcher.Item<Float> zeroHealth = new DataWatcher.Item<>(EntityLiving.HEALTH, 0f);

        if(packet.b != null) {
            for(int i = 0; i < packet.b.size(); i++) {
                DataWatcher.Item<?> item = packet.b.get(i);
                if(EntityLiving.HEALTH.equals(item.a())) {
                    packet.b.set(i, zeroHealth);
                    replaced = true;
                }
            }
        }

        if(!replaced) {
            if(packet.b == null) {
                packet.b = Collections.singletonList(zeroHealth);
            } else {
                packet.b.add(zeroHealth);
            }
        }

        sendPacketToViewers(player, packet);
    }

    public static void sendDeathEffects(Player player, boolean blackout) {
        sendPacket(player, new PacketPlayOutEntityEffect(player.getEntityId(),
                new MobEffect(MobEffectList.getByName("nausea"), 100, 0, true, false)));
        sendPacket(player, new PacketPlayOutEntityEffect(player.getEntityId(),
                new MobEffect(MobEffectList.getByName("blindness"), blackout ? Integer.MAX_VALUE : 21, 0, true, false)));
    }

    public static void clearDeathEffects(Player player) {
        sendPacket(player, new PacketPlayOutRemoveEntityEffect(player.getEntityId(), MobEffectList.getByName("nausea")));
        sendPacket(player, new PacketPlayOutRemoveEntityEffect(player.getEntityId(), MobEffectList.getByName("blindness")));
    }

    static ItemStack asNMS(org.bukkit.inventory.ItemStack bukkit) {
        if(bukkit instanceof CraftItemStack) {
            return ((CraftItemStack) bukkit).getHandle();
        } else {
            return CraftItemStack.asNMSCopy(bukkit);
        }
    }

    public static String getKey(Material material) {
        MinecraftKey key = Item.REGISTRY.b(CraftMagicNumbers.getItem(material));
        return key == null ? null : key.toString();
    }

    public static @Nullable Material materialByKey(String key) {
        final Item item = Item.REGISTRY.get(new MinecraftKey(key));
        return item == null ? null : CraftMagicNumbers.getMaterial(item);
    }

    public static String getTranslationKey(org.bukkit.inventory.ItemStack stack) {
        ItemStack nms = asNMS(stack);
        return nms == null || nms.getItem().equals(Items.a) // Items.a == AIR
               ? null
               : nms.getItem().j(nms) + ".name";
    }

    private static String getTranslationKey(Block nmsBlock) {
        return nmsBlock.a() + ".name";
    }

    // Some materials cannot be made into NMS ItemStacks (e.g. Lava),
    // so try to make them directly into blocks instead.
    private static @Nullable String getBlockTranslationKey(Material material) {
        Block nmsBlock = CraftMagicNumbers.getBlock(material);
        return nmsBlock == null ? null : getTranslationKey(nmsBlock);
    }

    public static @Nullable String getTranslationKey(Material material) {
        String key = getTranslationKey(new org.bukkit.inventory.ItemStack(material));
        return key != null ? key : getBlockTranslationKey(material);
    }

    public static @Nullable String getTranslationKey(MaterialData material) {
        String key = getTranslationKey(material.toItemStack(1));
        return key != null ? key : getBlockTranslationKey(material.getItemType());
    }

    public static String getTranslationKey(Entity entity) {
        if(entity instanceof TNTPrimed) {
            return "tile.tnt.name";
        } else if(entity instanceof Egg) {
            return "item.egg.name";
        } else {
            final String id = EntityTypes.b(((CraftEntity) entity).getHandle());
            return "entity." + (id != null ? id : "generic") + ".name";
        }
    }

    public static String getTranslationKey(PotionEffectType effect) {
        if(effect instanceof CraftPotionEffectType) {
            return ((CraftPotionEffectType) effect).getHandle().a();
        } else if(effect instanceof PotionEffectTypeWrapper) {
            return getTranslationKey(((PotionEffectTypeWrapper) effect).getType());
        } else {
            return "potion.empty";
        }
    }

    public static org.bukkit.enchantments.Enchantment getEnchantment(String key) {
        Enchantment enchantment = Enchantment.b(key);
        return enchantment == null ? null : org.bukkit.enchantments.Enchantment.getById(Enchantment.getId(enchantment));
    }

    public static Key getKey(org.bukkit.enchantments.Enchantment enchantment) {
        return CraftKey.get(Enchantment.enchantments.b(Enchantment.c(enchantment.getId())));
    }

    public static Set<MaterialData> getBlockStates(Material material) {
        ImmutableSet.Builder<MaterialData> materials = ImmutableSet.builder();
        Block nmsBlock = CraftMagicNumbers.getBlock(material);
        List<IBlockData> states = nmsBlock.s().a();
        if(states != null) {
            for(IBlockData state : states) {
                int data = nmsBlock.toLegacyData(state);
                materials.add(material.getNewData((byte) data));
            }
        }
        return materials.build();
    }

    public static String getKey(Sound bukkitSound) {
        return CraftSound.getSound(bukkitSound);
    }

    public static void useEntity(Player user, Entity target, boolean attack, EquipmentSlot hand) {
        ((CraftPlayer) user).getHandle().playerConnection.a(new PacketPlayInUseEntity(
            target.getEntityId(),
            attack ? PacketPlayInUseEntity.EnumEntityUseAction.ATTACK : PacketPlayInUseEntity.EnumEntityUseAction.INTERACT,
            null,
            hand == EquipmentSlot.OFF_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND
        ));
    }

    public static long lastTickDurationNanos() {
        // MinecraftServer.h is a circular buffer with the last 100 tick times
        // in nanoseconds, and MinecraftServer.aq() returns the iterator. The
        // iterator is incremented at the start of the tick, and the buffer is
        // written to at the end, so backing it up by one should give us the
        // most recent sample.
        int i = MinecraftServer.getServer().aq() % 100;
        if(--i < 0) i = 99;
        return MinecraftServer.getServer().h[i];
    }

    public static int playerLatencyMillis(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }
}
