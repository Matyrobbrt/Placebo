package shadows.placebo.events.client;

import io.github.matyrobbrt.eventdispatcher.Event;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import java.util.function.Supplier;

@FunctionalInterface
public interface RegisterLayerDefinitionsEvent extends Event {
    void register(ModelLayerLocation location, Supplier<LayerDefinition> definition);
}
