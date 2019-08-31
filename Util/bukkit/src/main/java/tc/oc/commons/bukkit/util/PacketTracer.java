package tc.oc.commons.bukkit.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.EnumProtocol;
import net.minecraft.server.EnumProtocolDirection;
import net.minecraft.server.IChatBaseComponent;
import net.minecraft.server.ItemStack;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;
import net.minecraft.server.PacketDataSerializer;
import net.minecraft.server.PacketDecoder;
import net.minecraft.server.PacketEncoder;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import tc.oc.commons.core.reflect.Fields;

/**
 * Dumps in/out packets to a logger by intercepting the low-level serialization methods.
 * This allows the fields of a packet to be displayed without any particular knowledge
 * of the packet structure.
 *
 * TODO: reader methods
 */
public class PacketTracer extends PacketDataSerializer {

    private static final Map<Class<? extends Packet>, Boolean> filter = new ConcurrentHashMap<>();
    private static boolean defaultInclude = true;

    private static final Set<Class<? extends Packet<?>>> packets;
    static {
        final TypeToken<Map<Class<? extends Packet<?>>, EnumProtocol>> fieldType = new TypeToken<Map<Class<? extends Packet<?>>, EnumProtocol>>(){};
        final Field field = Fields.oneOfType(EnumProtocol.class, fieldType);
        packets = Fields.read(field, fieldType, null).keySet();
    }

    /**
     * Find a packet class by case-insensitive substring of the class name.
     * If multiple packets match, the shortest name wins.
     */
    public static @Nullable Class<?> findPacketType(String name) {
        int minExtra = Integer.MAX_VALUE;
        Class<? extends Packet> best = null;
        name = name.toLowerCase();
        for(Class<? extends Packet> type : packets) {
            if(type.getName().toLowerCase().contains(name)) {
                int extra = type.getName().length() - name.length();
                if(extra < minExtra) {
                    best = type;
                    minExtra = extra;
                } else if(extra == minExtra) {
                    best = null;
                }
            }
        }
        return best;
    }

    /**
     * Start tracing packets for the given player
     */
    public static boolean start(Player player, Logger logger) {
        return start(getChannel(player), player.getName(), logger);
    }

    public static boolean start(Channel channel, String name, Logger logger) {
        final PacketEncoder oldEncoder = channel.pipeline().get(PacketEncoder.class);
        final PacketDecoder oldDecoder = channel.pipeline().get(PacketDecoder.class);

        channel.eventLoop().execute(() -> {
            if(channel.isOpen()) {
                if(oldEncoder != null) {
                    channel.pipeline().replace(oldEncoder, "encoder", new Encoder(logger, name));
                }

                if(oldDecoder != null) {
                    channel.pipeline().replace(oldDecoder, "decoder", new Decoder(logger, name));
                }
            }
        });

        return oldEncoder != null || oldDecoder != null;
    }

    /**
     * Stop tracing packets for the given player
     */
    public static boolean stop(Player player) {
        return stop(getChannel(player));
    }

    public static boolean stop(Channel channel) {
        final Encoder oldEncoder = channel.pipeline().get(Encoder.class);
        final Decoder oldDecoder = channel.pipeline().get(Decoder.class);

        channel.eventLoop().execute(() -> {
            if(channel.isOpen()) {
                if(oldEncoder != null) {
                    channel.pipeline().replace(oldEncoder, "encoder", new PacketEncoder(EnumProtocolDirection.CLIENTBOUND));
                }

                if(oldDecoder != null) {
                    channel.pipeline().replace(oldDecoder, "decoder", new PacketDecoder(EnumProtocolDirection.SERVERBOUND));
                }
            }
        });

        return oldEncoder != null || oldDecoder != null;
    }

    /**
     * Include or exclude the given packet class from tracing
     */
    public static void filter(Class<?> type, boolean include) {
        filter.put(type.asSubclass(Packet.class), include);
    }

    /**
     * Include or exclude the given packet class from tracing
     */
    public static boolean filter(String packetName, boolean include) {
        Class type = findPacketType(packetName);
        if(type == null) return false;
        filter(type, include);
        return true;
    }

    /**
     * Clear all packet inclusions and exclusions
     */
    public static void clearFilter() {
        filter.clear();
    }

    /**
     * Set whether packets are included or excluded by default
     */
    public static void setDefaultInclude(boolean include) {
        defaultInclude = include;
    }

    /**
     * Are packets included or excluded by default?
     */
    public static boolean getDefaultInclude() {
        return defaultInclude;
    }

    private static Channel getChannel(Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection.a().channel;
    }

    private static class Encoder extends MessageToByteEncoder<Packet> {
        private final Logger logger;
        private final String client;

        Encoder(Logger logger, String client) {
            this.logger = logger;
            this.client = client;
        }

        @Override
        protected void encode(ChannelHandlerContext context, Packet packet, ByteBuf buffer) throws Exception {
            final NetworkManager networkManager = context.pipeline().get(NetworkManager.class);
            final Integer id = context.channel().attr(NetworkManager.c).get().a(EnumProtocolDirection.CLIENTBOUND, packet);
            if (id == null) {
                throw new IOException("Cannot encode unregistered packet class " + packet.getClass());
            } else {
                try {
                    final PacketTracer dumper = new PacketTracer(buffer, logger, client);
                    dumper.d(id); // write VarInt
                    packet.b(dumper); // write packet
                    dumper.packet(">>>", id, packet);
                } catch (Throwable e) {
                    logger.log(Level.SEVERE, "Exception writing " + packet.getClass().getSimpleName(), e);
                }
            }
        }
    }

    private static class Decoder extends ByteToMessageDecoder {
        private final Logger logger;
        private final String client;

        Decoder(Logger logger, String client) {
            this.logger = logger;
            this.client = client;
        }

        @Override
        protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> messages) throws Exception {
            if(buffer.readableBytes() > 0) {
                final PacketTracer dumper = new PacketTracer(buffer, logger, client);
                final int id = dumper.g(); // read VarInt
                final NetworkManager networkManager = context.pipeline().get(NetworkManager.class);
                final Packet packet = context.channel().attr(NetworkManager.c).get().a(EnumProtocolDirection.SERVERBOUND, id);

                if (packet == null) {
                    throw new IOException("Cannot decode unregistered packet ID " + id);
                } else {
                    packet.a(dumper); // read packet

                    if (dumper.readableBytes() > 0) {
                        throw new IOException(dumper.readableBytes() + " extra bytes after reading packet " + packet.getClass().getSimpleName());
                    } else {
                        messages.add(packet);
                        dumper.packet("<<<", id, packet);
                    }
                }
            }
        }
    }

    private static final Joiner fieldJoiner = Joiner.on(' ');
    private static final Joiner listJoiner = Joiner.on(',');

    private final Logger log;
    private final String client;
    private final List<String> fields = new ArrayList<>();
    private boolean mute;

    public PacketTracer(ByteBuf buf, Logger logger, String client) {
        super(buf);
        this.log = logger;
        this.client = client;
    }

    private void field(String text) {
        if(!mute) {
            fields.add(text);
        }
    }

    private void value(String type, String value) {
        field(type + ":" + value);
    }

    private void value(String type, Object value) {
        field(type + ":" + String.valueOf(value));
    }

    private boolean value(boolean v) { value("boolean", String.valueOf(v)); return v; }
    private char value(char v) { value("char", String.valueOf(v)); return v; }
    private byte value(byte v) { value("byte", String.valueOf(v)); return v; }
    private short value(short v) { value("short", String.valueOf(v)); return v; }
    private int value(int v) { value("int", String.valueOf(v)); return v; }
    private long value(long v) { value("long", String.valueOf(v)); return v; }
    private float value(float v) { value("float", String.valueOf(v)); return v; }
    private double value(double v) { value("double", String.valueOf(v)); return v; }

    private short unsigned(short v) { value("ubyte", String.valueOf(v)); return v; }
    private int unsigned(int v) { value("ushort", String.valueOf(v)); return v; }
    private long unsigned(long v) { value("uint", String.valueOf(v)); return v; }

    private void list(String type, String...values) {
        field(type + ":[" + listJoiner.join(values) + "]");
    }

    private ByteBuf list(ByteBuf bytes, int start, int length) {
        final String[] raw = new String[length];
        for(int i = 0; i < length; i++) {
            raw[i] = Byte.toString(bytes.getByte(start + i));
        }
        list("byte", raw);
        return bytes;
    }

    private ByteBuf list(ByteBuf bytes, int start) {
        return list(bytes, start, bytes.readableBytes());
    }

    private byte[] list(byte[] bytes, int start, int length) {
        final String[] raw = new String[length];
        for(int i = 0; i < length; i++) {
            raw[i] = Byte.toString(bytes[start + i]);
        }
        list("byte", raw);
        return bytes;
    }

    private ByteBuffer list(ByteBuffer bytes, int start, int length) {
        final String[] raw = new String[length];
        for(int i = 0; i < length; i++) {
            raw[i] = Byte.toString(bytes.get(start + i));
        }
        list("byte", raw);
        return bytes;
    }

    private void packet(String dir, int id, Packet packet) {
        Boolean b = null;
        for(Map.Entry<Class<? extends Packet>, Boolean> entry : filter.entrySet()) {
            if(entry.getKey().isInstance(packet)) {
                b = entry.getValue();
                break;
            }
        }
        if(b == null ? defaultInclude : b) {
            log.info(client + " " + dir + " " + packet.getClass().getSimpleName() + "(" + id + ") {" + fieldJoiner.join(fields) + "}");
        }
    }

    @Override
    public PacketTracer a(ItemStack stack) {
        value("ItemStack", stack);
        try {
            mute = true;
            super.a(stack);
        } finally {
            mute = false;
        }
        return this;
    }

    @Override
    public PacketTracer a(IChatBaseComponent chat) {
        value("Chat", chat);
        try {
            mute = true;
            super.a(chat);
        } finally {
            mute = false;
        }
        return this;
    }

    @Override
    public PacketTracer a(BlockPosition pos) {
        value("BlockPosition", pos);
        try {
            mute = true;
            super.a(pos);
        } finally {
            mute = false;
        }
        return this;
    }

    @Override
    public PacketTracer a(NBTTagCompound nbt) {
        value("NBT", nbt);
        try {
            mute = true;
            super.a(nbt);
        } finally {
            mute = false;
        }
        return this;
    }

    @Override
    public PacketTracer a(UUID uuid) {
        value("UUID", uuid);
        try {
            mute = true;
            super.a(uuid);
        } finally {
            mute = false;
        }
        return this;
    }

    @Override
    public PacketDataSerializer a(String s) {
        value("String", StringEscapeUtils.escapeJava(s));
        try {
            mute = true;
            return super.a(s);
        } finally {
            mute = false;
        }
    }

    @Override
    public PacketTracer d(int i) {
        value("VarInt", Integer.toString(i));
        try {
            mute = true;
            super.d(i);
        } finally {
            mute = false;
        }
        return this;
    }

    @Override
    public PacketTracer b(long l) {
        value("VarInt", Long.toString(l));
        try {
            mute = true;
            super.b(l);
        } finally {
            mute = false;
        }
        return this;
    }

    @Override
    public PacketTracer a(Enum<?> e) {
        field(e.getClass().getSimpleName() + "." + e.name());
        try {
            mute = true;
            super.a(e);
        } finally {
            mute = false;
        }
        return this;
    }

    @Override
    public boolean readBoolean() {
        return value(super.readBoolean());
    }

    @Override
    public ByteBuf writeBoolean(boolean flag) {
        return super.writeBoolean(value(flag));
    }

    @Override
    public byte readByte() {
        return value(super.readByte());
    }

    @Override
    public short readUnsignedByte() {
        return unsigned(super.readUnsignedByte());
    }

    @Override
    public ByteBuf writeByte(int i) {
        value((byte) i);
        return super.writeByte(i);
    }

    @Override
    public short readShort() {
        return value(super.readShort());
    }

    @Override
    public int readUnsignedShort() {
        return unsigned(super.readUnsignedShort());
    }

    @Override
    public ByteBuf writeShort(int i) {
        return super.writeShort(value(i));
    }

    @Override
    public int readMedium() {
        final int i = super.readMedium();
        value("medium", String.valueOf(i));
        return i;
    }

    @Override
    public int readUnsignedMedium() {
        final int i = super.readMedium();
        value("umedium", String.valueOf(i));
        return i;
    }

    @Override
    public ByteBuf writeMedium(int i) {
        value("medium", String.valueOf(i));
        return super.writeMedium(i);
    }

    @Override
    public int readInt() {
        return value(super.readInt());
    }

    @Override
    public long readUnsignedInt() {
        return unsigned(super.readUnsignedInt());
    }

    @Override
    public ByteBuf writeInt(int i) {
        return super.writeInt(value(i));
    }

    @Override
    public long readLong() {
        return value(super.readLong());
    }

    @Override
    public ByteBuf writeLong(long i) {
        return super.writeLong(value(i));
    }

    @Override
    public char readChar() {
        return value(super.readChar());
    }

    @Override
    public ByteBuf writeChar(int i) {
        return super.writeChar(value((char) i));
    }

    @Override
    public float readFloat() {
        return value(super.readFloat());
    }

    @Override
    public ByteBuf writeFloat(float f) {
        return super.writeFloat(value(f));
    }

    @Override
    public double readDouble() {
        return value(super.readDouble());
    }

    @Override
    public ByteBuf writeDouble(double d) {
        return super.writeDouble(value(d));
    }

    @Override
    public ByteBuf readBytes(ByteBuf bytebuf) {
        return super.readBytes(bytebuf);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf bytes) {
        return writeBytes(bytes, bytes.readableBytes());
    }

    @Override
    public ByteBuf readBytes(ByteBuf bytes, int length) {
        return readBytes(bytes, bytes.writerIndex(), length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf bytes, int length) {
        return writeBytes(bytes, bytes.readerIndex(), length);
    }

    @Override
    public ByteBuf readBytes(ByteBuf bytebuf, int start, int length) {
        return list(super.readBytes(bytebuf, start, length), start, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf bytes, int start, int length) {
        return super.writeBytes(list(bytes, start, length), start, length);
    }

    @Override
    public ByteBuf readBytes(byte[] bytes) {
        return readBytes(bytes, 0, bytes.length);
    }

    @Override
    public ByteBuf writeBytes(byte[] bytes) {
        return writeBytes(bytes, 0, bytes.length);
    }

    @Override
    public ByteBuf readBytes(byte[] bytes, int start, int length) {
        return list(super.readBytes(bytes, start, length), start, length);
    }

    @Override
    public ByteBuf writeBytes(byte[] bytes, int start, int length) {
        list(bytes, start, length);
        return super.writeBytes(bytes, start, length);
    }

    @Override
    public ByteBuf readBytes(int i) {
        return list(super.readBytes(i), 0);
    }

    @Override
    public ByteBuf writeBytes(ByteBuffer bytes) {
        list(bytes, 0, bytes.remaining());
        return super.writeBytes(bytes);
    }

    @Override
    public int writeBytes(InputStream istream, int max) throws IOException {
        byte[] bytes = new byte[max];
        int length = istream.read(bytes, 0, max);
        writeBytes(bytes, 0, length);
        return length;
    }

    @Override
    public int writeBytes(ScatteringByteChannel ch, int max) throws IOException {
        ByteBuffer tmp = ByteBuffer.allocateDirect(max);
        int length = ch.read(tmp);
        writeBytes(tmp);
        return length;
    }

    @Override
    public ByteBuf writeZero(int i) {
        list("byte", "0 * " + i);
        return super.writeZero(i);
    }
}
