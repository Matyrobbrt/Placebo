package shadows.placebo.forge.client;

import io.github.matyrobbrt.eventdispatcher.SubscribeEvent;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import shadows.placebo.Placebo;
import shadows.placebo.PlaceboClient;
import shadows.placebo.events.RegisterReloadListenersEvent;
import shadows.placebo.events.client.ClientSetupEvent;
import shadows.placebo.events.client.ClientTickEvent;
import shadows.placebo.events.client.RegisterKeysEvent;
import shadows.placebo.events.client.RegisterLayerDefinitionsEvent;
import shadows.placebo.events.client.RegisterLayersEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlaceboForgeClient {
    @SubscribeEvent
    static void onClientTick(final TickEvent.ClientTickEvent event) {
        Placebo.BUS.post(new ClientTickEvent(event.phase == TickEvent.Phase.END ? ClientTickEvent.Phase.END : ClientTickEvent.Phase.START));
    }

    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = Placebo.MODID)
    public static final class Mod {
        @SubscribeEvent
        static void setup(final FMLConstructModEvent $event) {
            PlaceboClient.init();
            MinecraftForge.EVENT_BUS.addListener((final RegisterClientReloadListenersEvent event) ->
                    Placebo.BUS.post(new RegisterReloadListenersEvent(PackType.CLIENT_RESOURCES, null, (location, preparableReloadListener) -> event.registerReloadListener(preparableReloadListener))));
        }

        @SubscribeEvent
        static void onClientSetup(final FMLClientSetupEvent event) {
            Placebo.BUS.post(new ClientSetupEvent(event::enqueueWork));
            event.enqueueWork(() -> Placebo.BUS.post(new RegisterLayerDefinitionsEvent(
                    ForgeHooksClient::registerLayerDefinition
            )));
        }

        @SubscribeEvent
        static void addLayers(final EntityRenderersEvent.AddLayers event) {
            Placebo.BUS.post(new RegisterLayersEvent(
                    entityType -> event.getRenderer((EntityType) entityType), event::getSkin, event.getSkins()
            ));
        }

        @SubscribeEvent
        static void registerKeymappings(final RegisterKeyMappingsEvent event) {
            Placebo.BUS.post(new RegisterKeysEvent(event::register));
        }
    }
}
