package shadows.placebo.events;

import com.mojang.brigadier.CommandDispatcher;
import io.github.matyrobbrt.eventdispatcher.Event;
import net.minecraft.commands.CommandSourceStack;

public record RegisterCommandsEvent(CommandDispatcher<CommandSourceStack> dispatcher) implements Event {
}
