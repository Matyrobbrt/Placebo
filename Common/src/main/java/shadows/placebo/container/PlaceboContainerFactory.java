package shadows.placebo.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface PlaceboContainerFactory<T extends AbstractContainerMenu> extends MenuType.MenuSupplier<T> {
    T create(int windowId, Inventory inv, FriendlyByteBuf data);

    @Override
    default T create(int id, Inventory inv) {
        return create(id, inv, null);
    }
}
