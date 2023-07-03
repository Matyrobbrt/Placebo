package shadows.placebo.events.client;

import io.github.matyrobbrt.eventdispatcher.Event;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public record RegisterLayerDefinitionsEvent(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> registrar) implements Event {
}
