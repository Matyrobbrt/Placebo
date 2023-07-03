package shadows.placebo.events;

import io.github.matyrobbrt.eventdispatcher.Event;
import net.minecraft.server.level.ServerPlayer;

public record DatapackSyncEvent(ServerPlayer player) implements Event {
}
