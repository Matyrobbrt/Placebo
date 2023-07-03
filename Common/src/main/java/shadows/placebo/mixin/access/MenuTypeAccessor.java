package shadows.placebo.mixin.access;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MenuType.class)
public interface MenuTypeAccessor<T extends AbstractContainerMenu>  {
    @Accessor
    MenuType.MenuSupplier<T> getConstructor();
}
