package tc.oc.minecraft.protocol;

import java.util.Map;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

public enum MinecraftVersion {

    MINECRAFT_1_7_2(4, "1.7.2"),
    MINECRAFT_1_7_10(5, "1.7.10"),
    MINECRAFT_1_8(47, "1.8"),
    MINECRAFT_1_9(107, "1.9"),
    MINECRAFT_1_9_1(108, "1.9.1"),
    MINECRAFT_1_9_2(109, "1.9.2"),
    MINECRAFT_1_9_4(110, "1.9.4"),
    MINECRAFT_1_10(210, "1.10"),
    MINECRAFT_1_11(315, "1.11"),
    MINECRAFT_1_11_1(316, "1.11.1"),
    MINECRAFT_1_12(335, "1.12");

    private final int protocol;
    private final String version;

    MinecraftVersion(int protocol, String version) {
        this.protocol = protocol;
        this.version = version;
    }

    public int protocol() {
        return protocol;
    }

    public String version() {
        return version;
    }

    public static String describeProtocol(int protocol) {
        final MinecraftVersion mv = byProtocol(protocol);
        return mv != null ? mv.version()
                          : "unknown." + protocol;
    }

    public static @Nullable MinecraftVersion byProtocol(int protocol) {
        return byProtocol.get(protocol);
    }

    public static boolean atLeast(MinecraftVersion version, int protocol) {
        MinecraftVersion other = byProtocol(protocol);
        return other == null || other.protocol() >= version.protocol();
    }

    public static boolean lessThan(MinecraftVersion version, int protocol) {
        return !atLeast(version, protocol);
    }

    private static final Map<Integer, MinecraftVersion> byProtocol;

    static {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        for(MinecraftVersion version : values()) {
            builder.put(version.protocol(), version);
        }
        byProtocol = builder.build();
    }
}
