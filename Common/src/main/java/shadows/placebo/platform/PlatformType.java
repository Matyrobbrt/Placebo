package shadows.placebo.platform;

public enum PlatformType {
    FORGE {
        @Override
        public String getConditionsName() {
            return "forge:conditions";
        }
    },
    FABRIC {
        @Override
        public String getConditionsName() {
            return "fabric:load_conditions";
        }
    };

    public abstract String getConditionsName();
}
