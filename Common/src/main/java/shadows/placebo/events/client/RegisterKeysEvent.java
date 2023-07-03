package shadows.placebo.events.client;

import io.github.matyrobbrt.eventdispatcher.Event;
import net.minecraft.client.KeyMapping;

import java.util.function.Consumer;

public record RegisterKeysEvent(Consumer<KeyMapping> registrar) implements Event {
    public void register(KeyMapping mapping) {
        registrar.accept(mapping);
    }
}
