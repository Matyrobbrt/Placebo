package shadows.placebo.fabric.api;

import io.github.matyrobbrt.eventdispatcher.EventBus;

public interface PlaceboInitEntrypoint {
    void run(EventBus bus);
}
