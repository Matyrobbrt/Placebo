package shadows.placebo.mixin;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shadows.placebo.util.PlaceboTaskQueue;

import java.util.function.BooleanSupplier;

@Mixin({GameTestServer.class, IntegratedServer.class, DedicatedServer.class})
public class MinecraftServersMixin {
    @Inject(at = @At("RETURN"), method = "initServer()Z")
    private void placebo$onInitServer(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            PlaceboTaskQueue.started((MinecraftServer) (Object) this);
        }
    }
}

@Mixin(MinecraftServer.class)
class MinecraftServerMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;onServerExit()V"), method = "runServer")
    private void placebo$onServerClose(CallbackInfo ci) {
        PlaceboTaskQueue.stopped();
    }

    @Inject(at = @At("TAIL"), method = "tickServer")
    private void placebo$onTickServer(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        PlaceboTaskQueue.tick();
    }
}
