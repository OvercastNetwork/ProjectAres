package tc.oc.minecraft.protocol;

import java.util.Map;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

public enum MinecraftVersion {

    MINECRAFT_1_4_7(51, 1, 4, 7),
    MINECRAFT_1_5_1(60, 1, 5, 1),
    MINECRAFT_1_5_2(61, 1, 5, 2),
    MINECRAFT_1_6_1(73, 1, 6, 1),
    MINECRAFT_1_6_2(74, 1, 6, 2),
    MINECRAFT_1_6_4(78, 1, 6, 4),
    MINECRAFT_1_7_2(4, 1, 7, 2),
    MINECRAFT_1_7_10(5, 1, 7, 10),
    MINECRAFT_1_8(47, 1, 8, 0),
    MINECRAFT_1_9(107, 1, 9, 0),
    MINECRAFT_1_9_1(108, 1, 9 , 1),
    MINECRAFT_1_9_2(109, 1, 9 , 2),
    MINECRAFT_1_9_4(110, 1, 9 , 4),
    MINECRAFT_1_10(210, 1, 10, 0),
    MINECRAFT_1_11(315, 1, 11, 0),
    MINECRAFT_1_11_1(316, 1, 11, 1),
    MINECRAFT_1_12(335, 1, 12, 0),
    MINECRAFT_1_12_1(338, 1, 12, 1),
    MINECRAFT_1_12_2(340, 1, 12, 2);

    private final int protocol;
    private final int major;
    private final int minor;
    private final int patch;

    MinecraftVersion(int protocol, int major, int minor, int patch) {
        this.protocol = protocol;
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int protocol() {
        return protocol;
    }

    public String version() {
        return major + "." + minor + (patch != 0 ? "." + patch : "");
    }

    public String simplifiedVersion() {
        return major + "." + minor;
    }

    public static String describeProtocol(int protocol) {
        return describeProtocol(protocol, false);
    }

    public static String describeProtocol(int protocol, boolean simplified) {
        final MinecraftVersion mv = byProtocol(protocol);
        return mv != null ? (simplified ? mv.simplifiedVersion() : mv.version())
                : "unknown." + protocol;
    }

    public static @Nullable MinecraftVersion byProtocol(int protocol) {
        return byProtocol.get(protocol);
    }

    public static boolean atLeast(MinecraftVersion version, int protocol) {
        MinecraftVersion other = byProtocol(protocol);
        return other == null || other.ordinal() >= version.ordinal();
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
