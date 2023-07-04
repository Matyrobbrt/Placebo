package shadows.placebo.forge.client;

import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import shadows.placebo.Placebo;
import shadows.placebo.PlaceboClient;
import shadows.placebo.events.ModEventBus;
import shadows.placebo.events.RegisterReloadListenersEvent;
import shadows.placebo.events.client.ClientSetupEvent;
import shadows.placebo.events.client.ClientTickEvent;
import shadows.placebo.events.client.RegisterAdditionalModelsEvent;
import shadows.placebo.events.client.RegisterKeysEvent;
import shadows.placebo.events.client.RegisterLayerDefinitionsEvent;
import shadows.placebo.events.client.RegisterLayersEvent;
import shadows.placebo.events.client.RegisterOverlaysEvent;

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
            Placebo.BUS.<ClientSetupEvent>post(event::enqueueWork);
            event.enqueueWork(() -> Placebo.BUS.<RegisterLayerDefinitionsEvent>post(ForgeHooksClient::registerLayerDefinition));
        }

        @SubscribeEvent
        static void addLayers(final EntityRenderersEvent.AddLayers event) {
            Placebo.BUS.post(new RegisterLayersEvent(
                    entityType -> event.getRenderer((EntityType) entityType), event::getSkin, event.getSkins()
            ));
        }

        @SubscribeEvent
        static void registerKeymappings(final RegisterKeyMappingsEvent event) {
            Placebo.BUS.<RegisterKeysEvent>post(event::register);
        }

        @SubscribeEvent
        static void registerOverlays(final RegisterGuiOverlaysEvent event) {
            final var ctx = ModLoadingContext.get();
            final var oldContainer = ctx.getActiveContainer();
            ModEventBus.forEachBus((modId, bus) -> {
                ctx.setActiveContainer(ModList.get().getModContainerById(modId).orElseThrow());
                bus.<RegisterOverlaysEvent>post((id, overlay) ->
                        event.registerAboveAll(id, overlay::render));
            });
            ctx.setActiveContainer(oldContainer);
        }

        @SubscribeEvent
        static void registerItemColors(final RegisterColorHandlersEvent.Item event) {
            Placebo.BUS.<shadows.placebo.events.client.RegisterColorHandlersEvent.Item>post(event::register);
        }

        @SubscribeEvent
        static void registerAdditional(ModelEvent.RegisterAdditional event) {
            Placebo.BUS.<RegisterAdditionalModelsEvent>post(event::register);
        }
    }
}
