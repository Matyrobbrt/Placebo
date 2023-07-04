package shadows.placebo.events.client;

import io.github.matyrobbrt.eventdispatcher.Event;
import net.minecraft.client.KeyMapping;

@FunctionalInterface
public interface RegisterKeysEvent extends Event {
    void register(KeyMapping mapping);
}
