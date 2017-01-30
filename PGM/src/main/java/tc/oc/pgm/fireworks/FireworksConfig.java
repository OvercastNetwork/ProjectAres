package tc.oc.pgm.fireworks;

import tc.oc.pgm.Config;

public class FireworksConfig {
    public static class PostMatch {
        public static boolean enabled() {
            return Config.getConfiguration().getBoolean("fireworks.post-match.enabled", false);
        }

        public static int number() {
            return Math.max(1, Config.getConfiguration().getInt("fireworks.post-match.number", 5));
        }

        public static int delay() {
            return Math.max(0, Config.getConfiguration().getInt("fireworks.post-match.delay", 40));
        }

        public static int frequency() {
            return Math.max(1, Config.getConfiguration().getInt("fireworks.post-match.frequency", 40));
        }

        public static int iterations() {
            return Math.max(1, Config.getConfiguration().getInt("fireworks.post-match.iterations", 15));
        }

        public static int power() {
            return Math.max(0, Config.getConfiguration().getInt("fireworks.post-match.power", 2));
        }
    }

    public static abstract class Goals {
        public static boolean enabled() {
            return Config.getConfiguration().getBoolean("fireworks.goals.enabled", false);
        }
    }
}
