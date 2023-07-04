package shadows.placebo.events.client;

import io.github.matyrobbrt.eventdispatcher.Event;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.level.ItemLike;

@FunctionalInterface
public interface RegisterColorHandlersEvent<COLOR, TARGET> {
    @SuppressWarnings("unchecked")
    void register(COLOR color, TARGET... targets);

    @FunctionalInterface
    interface Item extends Event, RegisterColorHandlersEvent<ItemColor, ItemLike> {
    }
}
