package shadows.placebo.events.client;

import io.github.matyrobbrt.eventdispatcher.Event;
import io.github.matyrobbrt.eventdispatcher.internal.EventDispatcher;

public record ClientTickEvent(Phase phase) implements Event {
    public static final EventDispatcher DISPATCHER = new EventDispatcher();

    @Override
    public EventDispatcher getDispatcher() {
        return DISPATCHER;
    }

    public enum Phase {
        START, END
    }

}
