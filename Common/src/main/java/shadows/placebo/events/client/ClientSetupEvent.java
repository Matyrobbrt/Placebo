package shadows.placebo.events.client;

import io.github.matyrobbrt.eventdispatcher.Event;

import java.util.function.Consumer;

@FunctionalInterface
public interface ClientSetupEvent extends Event {
    void enqueue(Runnable runnable);
}
