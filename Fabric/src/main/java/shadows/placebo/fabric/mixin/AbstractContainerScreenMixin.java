package shadows.placebo.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import shadows.placebo.screen.PlaceboContainerScreen;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDIII)Z"), method = "mouseReleased")
    private boolean placebo$dontDropOutside(
            AbstractContainerScreen<T> owner, double a, double b, int c, int d, int e, Operation<Boolean> operation, @Local(ordinal = 0) Slot slot
    ) {
        if (((Object)this) instanceof PlaceboContainerScreen<?>) {
            if (slot != null) {
                return false;
            }
        }
        return operation.call(owner, a, b, c, d, e);
    }
}
