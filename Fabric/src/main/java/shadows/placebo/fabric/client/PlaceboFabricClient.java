package shadows.placebo.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import shadows.placebo.Placebo;
import shadows.placebo.PlaceboClient;
import shadows.placebo.events.ModEventBus;
import shadows.placebo.events.client.ClientSetupEvent;
import shadows.placebo.events.client.ClientTickEvent;
import shadows.placebo.events.client.RegisterAdditionalModelsEvent;
import shadows.placebo.events.client.RegisterColorHandlersEvent;
import shadows.placebo.events.client.RegisterKeysEvent;
import shadows.placebo.events.client.RegisterLayerDefinitionsEvent;
import shadows.placebo.events.client.RegisterOverlaysEvent;
import shadows.placebo.fabric.api.PlaceboClientInitEntrypoint;

import java.util.ArrayList;
import java.util.List;

public class PlaceboFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricLoader.getInstance().getEntrypointContainers("placeboClientInit", PlaceboClientInitEntrypoint.class)
                .forEach(container -> container.getEntrypoint().run(ModEventBus.grabBus(container.getProvider().getMetadata().getId())));
        PlaceboClient.init();
        Placebo.BUS.<RegisterKeysEvent>post(KeyBindingHelper::registerKeyBinding);
        ClientTickEvents.START_CLIENT_TICK.register(client -> Placebo.BUS.post(
                new ClientTickEvent(ClientTickEvent.Phase.START)
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> Placebo.BUS.post(
                new ClientTickEvent(ClientTickEvent.Phase.END)
        ));
        Placebo.BUS.<RegisterLayerDefinitionsEvent>post(
                (modelLayerLocation, layerDefinitionSupplier) -> EntityModelLayerRegistry.registerModelLayer(modelLayerLocation, layerDefinitionSupplier::get)
        );

        final List<RegisterOverlaysEvent.GuiOverlay> overlays = new ArrayList<>();
        ModEventBus.forEachBus((mid, bus) -> bus.<RegisterOverlaysEvent>post((id, overlay) -> overlays.add(overlay)));
        HudRenderCallback.EVENT.register((graphics, partialTick) -> {
            for (final RegisterOverlaysEvent.GuiOverlay overlay : overlays) {
                overlay.render(Minecraft.getInstance().gui, graphics, partialTick, graphics.guiWidth(), graphics.guiHeight());
            }
        });
        Placebo.BUS.<RegisterColorHandlersEvent.Item>post(ColorProviderRegistry.ITEM::register);
        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, paths) -> Placebo.BUS.<RegisterAdditionalModelsEvent>post(paths::accept));

        Placebo.BUS.<ClientSetupEvent>post(Runnable::run);
    }
}
