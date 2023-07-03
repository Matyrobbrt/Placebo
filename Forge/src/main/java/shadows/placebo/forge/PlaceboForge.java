package shadows.placebo.forge;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;
import shadows.placebo.Constants;
import shadows.placebo.Placebo;
import shadows.placebo.events.CommonSetupEvent;
import shadows.placebo.events.DatapackSyncEvent;
import shadows.placebo.events.ModEventBus;
import shadows.placebo.events.PlayerTickEvent;
import shadows.placebo.events.RegisterReloadListenersEvent;
import shadows.placebo.json.ConditionHelper;
import shadows.placebo.util.RegistryEvent;

import java.lang.ref.WeakReference;
import java.util.Objects;

@Mod(Constants.MOD_ID)
@SuppressWarnings("all")
public class PlaceboForge {

    public PlaceboForge() {
        Placebo.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener((final RegisterEvent e) -> {
            final var type = RegistryEvent.REGISTRIES_TO_FIRE.get(e.getRegistryKey());
            if (type != null) {
                final var ctx = ModLoadingContext.get();
                final var prevContainer = ctx.getActiveContainer();
                ModEventBus.forEachBus((modId, bus) -> {
                    ctx.setActiveContainer(ModList.get().getModContainerById(modId).orElseThrow());
                    bus.post(new RegistryEvent.Register<>(
                            type, e.getVanillaRegistry(),
                            e.getForgeRegistry() == null ? new RegistryEvent.RegistryWrapper() {

                                @Override
                                public void register(Object object, String id) {
                                    Registry.register(e.getVanillaRegistry(), new ResourceLocation(modId, id), object);
                                }

                                @Override
                                public void register(Object object, ResourceLocation id) {
                                    Registry.register(e.getVanillaRegistry(), id, object);
                                }
                            } : new RegistryEvent.RegistryWrapper() {

                                @Override
                                public void register(Object object, String id) {
                                    e.getForgeRegistry().register(new ResourceLocation(modId, id), object);
                                }

                                @Override
                                public void register(Object object, ResourceLocation id) {
                                    e.getForgeRegistry().register(id, object);
                                }
                            }
                    ));
                });
                ctx.setActiveContainer(prevContainer);
            }
        });

        MinecraftForge.EVENT_BUS.addListener((final RegisterCommandsEvent event) ->
                Placebo.BUS.post(new shadows.placebo.events.RegisterCommandsEvent(event.getDispatcher())));

        MinecraftForge.EVENT_BUS.addListener((final TickEvent.PlayerTickEvent event) ->
                Placebo.BUS.post(new PlayerTickEvent(event.player, event.phase == TickEvent.Phase.START ? PlayerTickEvent.Phase.START : PlayerTickEvent.Phase.END)));

        MinecraftForge.EVENT_BUS.addListener((final AddReloadListenerEvent event) ->
                Placebo.BUS.post(new RegisterReloadListenersEvent(PackType.SERVER_DATA, new ConditionHelper() {
                    private final WeakReference<ICondition.IContext> context = new WeakReference<>(event.getConditionContext());
                    @Override
                    public boolean shouldBeLoaded(JsonObject object, String conditionsArrayName, Logger logger) {
                        return CraftingHelper.processConditions(object, conditionsArrayName, Objects.requireNonNullElse(context.get(), ICondition.IContext.EMPTY));
                    }
                }, (location, preparableReloadListener) -> event.addListener(preparableReloadListener))));

        MinecraftForge.EVENT_BUS.addListener((final OnDatapackSyncEvent event) -> {
            if (event.getPlayer() == null) {
                event.getPlayerList().getPlayers().forEach(player ->
                        Placebo.BUS.post(new DatapackSyncEvent(player)));
            } else {
                Placebo.BUS.post(new DatapackSyncEvent(event.getPlayer()));
            }
        });

        FMLJavaModLoadingContext.get().getModEventBus().addListener((final FMLCommonSetupEvent event) ->
                Placebo.BUS.post(new CommonSetupEvent(event::enqueueWork)));
    }
}