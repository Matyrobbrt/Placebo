package shadows.placebo.mixin.access;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor<T extends LivingEntity, M extends EntityModel<T>> {
    @Invoker("addLayer")
    boolean placebo$addLayer(RenderLayer<T, M> renderLayer);
}
