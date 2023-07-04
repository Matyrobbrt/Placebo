package shadows.placebo.events.client;

import io.github.matyrobbrt.eventdispatcher.Event;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface RegisterAdditionalModelsEvent extends Event {
    void register(ResourceLocation location);
}
