package shadows.placebo.screen;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public interface ScreenFactory<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> extends MenuScreens.ScreenConstructor<T, U> {
    @Override
    U create(T abstractContainerMenu, Inventory inventory, Component component);
}
