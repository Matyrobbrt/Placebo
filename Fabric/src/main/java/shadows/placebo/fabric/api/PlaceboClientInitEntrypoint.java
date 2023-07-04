package shadows.placebo.fabric.api;

import io.github.matyrobbrt.eventdispatcher.EventBus;

public interface PlaceboClientInitEntrypoint {
    void run(EventBus bus);
}
