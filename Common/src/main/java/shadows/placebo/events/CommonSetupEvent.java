package shadows.placebo.events;

import io.github.matyrobbrt.eventdispatcher.Event;

import java.util.function.Consumer;

public class CommonSetupEvent implements Event {
    private final Consumer<Runnable> enqueue;

    public CommonSetupEvent(Consumer<Runnable> enqueue) {
        this.enqueue = enqueue;
    }

    public void enqueue(Runnable task) {
        enqueue.accept(task);
    }
}
