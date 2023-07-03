package shadows.placebo.fabric.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shadows.placebo.Placebo;
import shadows.placebo.events.PlayerTickEvent;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(at = @At("HEAD"), method = "tick()V")
    private void placebo$startTick(CallbackInfo ci) {
        Placebo.BUS.post(new PlayerTickEvent((Player) (Object) this, PlayerTickEvent.Phase.START));
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    private void placebo$endTick(CallbackInfo ci) {
        Placebo.BUS.post(new PlayerTickEvent((Player) (Object) this, PlayerTickEvent.Phase.END));
    }
}
