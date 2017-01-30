package tc.oc.pgm.antigrief;

import tc.oc.pgm.Config;

public class AntiGrief {
    public static class Defuse {
        public static boolean enabled() {
            return Config.getConfiguration().getBoolean("antigrief.diffuse.enabled", false);
        }
    }

    public static class CraftProtect {
        public static boolean enabled() {
            return Config.getConfiguration().getBoolean("antigrief.craft-protect.enabled", false);
        }
    }

    public static class VechicleProtect {
        public static boolean enabled() {
            return Config.getConfiguration().getBoolean("antigrief.vechicle-protect.enabled", false);
        }
    }

    public static class AnvilProtect {
        public static boolean enabled() {
            return Config.getConfiguration().getBoolean("antigrief.anvil-protect.enabled", false);
        }
    }
}
