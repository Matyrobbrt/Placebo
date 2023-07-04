package shadows.placebo.packets;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import shadows.placebo.container.PlaceboContainerFactory;
import shadows.placebo.mixin.access.MenuScreensAccessor;
import shadows.placebo.mixin.access.MenuTypeAccessor;
import shadows.placebo.network.MessageProvider;
import shadows.placebo.network.NetworkContext;

import java.util.function.Supplier;

public class OpenContainer implements MessageProvider<OpenContainer> {
    private ResourceLocation id;
    private int windowId;
    private Component name;
    private FriendlyByteBuf additionalData;

    public OpenContainer(MenuType<?> id, int windowId, Component name, FriendlyByteBuf additionalData) {
        this(BuiltInRegistries.MENU.getKey(id), windowId, name, additionalData);
    }

    public OpenContainer(ResourceLocation id, int windowId, Component name, FriendlyByteBuf additionalData) {
        this.id = id;
        this.windowId = windowId;
        this.name = name;
        this.additionalData = additionalData;
    }

    public OpenContainer() {}

    @Override
    public void write(OpenContainer msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.id);
        buf.writeVarInt(msg.windowId);
        buf.writeComponent(msg.name);
        buf.writeByteArray(msg.additionalData.readByteArray());
    }

    @Override
    public OpenContainer read(FriendlyByteBuf buf) {
        return new OpenContainer(buf.readResourceLocation(), buf.readVarInt(), buf.readComponent(), new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray(32600))));
    }

    @Override
    public void handle(OpenContainer msg, Supplier<NetworkContext> ctx) {
        msg.handleClient();
    }

    private void handleClient() {
        try {
            final var menu = BuiltInRegistries.MENU.get(id);
            final var inv = Minecraft.getInstance().player.getInventory();
            final var factory = (PlaceboContainerFactory) ((MenuTypeAccessor) menu).getConstructor();
            final var container = factory.create(windowId, inv, getAdditionalData());
            Minecraft.getInstance().player.containerMenu = container;
            Minecraft.getInstance().setScreen(((MenuScreens.ScreenConstructor)MenuScreensAccessor.placebo$getConstructor(menu)).create(
                    container, inv, getName()
            ));
        } finally {
            getAdditionalData().release();
        }
    }

    public int getWindowId() {
        return windowId;
    }

    public Component getName() {
        return name;
    }

    public FriendlyByteBuf getAdditionalData() {
        return additionalData;
    }
}
