package shadows.placebo.fabric.mixin;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shadows.placebo.Placebo;
import shadows.placebo.events.client.RegisterLayersEvent;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Shadow private Map<EntityType<?>, EntityRenderer<?>> renderers;

    @Shadow private Map<String, EntityRenderer<? extends Player>> playerRenderers;

    @Inject(at = @At("TAIL"), method = "onResourceManagerReload")
    public void placebo$registerLayers(ResourceManager resourceManager, CallbackInfo ci) {
        Placebo.BUS.post(new RegisterLayersEvent(
                renderers::get, playerRenderers::get, playerRenderers.keySet()
        ));
    }
}
