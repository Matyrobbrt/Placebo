package shadows.placebo.events.client;

import io.github.matyrobbrt.eventdispatcher.Event;

import java.util.function.Consumer;

public class ClientSetupEvent implements Event {
    private final Consumer<Runnable> enqueue;

    public ClientSetupEvent(Consumer<Runnable> enqueue) {
        this.enqueue = enqueue;
    }

    public void enqueue(Runnable task) {
        enqueue.accept(task);
    }
}
