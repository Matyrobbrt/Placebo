package shadows.placebo.events;

import io.github.matyrobbrt.eventdispatcher.Event;
import io.github.matyrobbrt.eventdispatcher.internal.EventDispatcher;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record PlayerTickEvent(Player player, Phase phase) implements Event {
    public static final EventDispatcher DISPATCHER = new EventDispatcher();

    @Override
    public EventDispatcher getDispatcher() {
        return DISPATCHER;
    }

    public boolean isClient() {
        return player instanceof ServerPlayer;
    }

    public enum Phase {
        START, END
    }

}
