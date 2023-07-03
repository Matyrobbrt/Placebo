package shadows.placebo.events;

import io.github.matyrobbrt.eventdispatcher.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ModEventBus {
    private static final Map<String, EventBus> BUSES = new HashMap<>();
    public static synchronized EventBus grabBus(String modId) {
        return BUSES.computeIfAbsent(modId, k -> {
            final EventBus bus = EventBus.builder(modId + " mod bus")
                    .build();
            bus.start();
            return bus;
        });
    }

    public static void forEachBus(BiConsumer<String, EventBus> consumer) {
        BUSES.forEach(consumer);
    }
}
