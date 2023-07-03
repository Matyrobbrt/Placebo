package shadows.placebo.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import shadows.placebo.Placebo;
import shadows.placebo.PlaceboClient;
import shadows.placebo.events.client.ClientSetupEvent;
import shadows.placebo.events.client.ClientTickEvent;
import shadows.placebo.events.client.RegisterKeysEvent;
import shadows.placebo.events.client.RegisterLayerDefinitionsEvent;

public class PlaceboFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PlaceboClient.init();
        Placebo.BUS.post(new ClientSetupEvent(Runnable::run));
        Placebo.BUS.post(new RegisterKeysEvent(KeyBindingHelper::registerKeyBinding));
        ClientTickEvents.START_CLIENT_TICK.register(client -> Placebo.BUS.post(
                new ClientTickEvent(ClientTickEvent.Phase.START)
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> Placebo.BUS.post(
                new ClientTickEvent(ClientTickEvent.Phase.END)
        ));
        Placebo.BUS.post(new RegisterLayerDefinitionsEvent(
                (modelLayerLocation, layerDefinitionSupplier) -> EntityModelLayerRegistry.registerModelLayer(modelLayerLocation, layerDefinitionSupplier::get)
        ));
    }
}
