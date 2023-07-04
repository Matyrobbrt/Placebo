package shadows.placebo.events;

import io.github.matyrobbrt.eventdispatcher.Event;

@FunctionalInterface
public interface CommonSetupEvent extends Event {
    void enqueue(Runnable runnable);
}
