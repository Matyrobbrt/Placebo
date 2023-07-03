package shadows.placebo.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shadows.placebo.Placebo;
import shadows.placebo.events.AnvilLandEvent;

@Mixin(AnvilBlock.class)
public class AnvilBlockMixin {

	@Inject(at = @At("TAIL"), method = "onLand", require = 1)
	public void onLand(Level level, BlockPos pos, BlockState newState, BlockState oldState, FallingBlockEntity entity, CallbackInfo ci) {
		Placebo.BUS.post(new AnvilLandEvent(level, pos, newState, oldState, entity));
	}

}
