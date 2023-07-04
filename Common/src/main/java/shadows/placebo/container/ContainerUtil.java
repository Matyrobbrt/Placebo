package shadows.placebo.container;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import shadows.placebo.Placebo;
import shadows.placebo.packets.OpenContainer;
import shadows.placebo.platform.Services;
import shadows.placebo.transfer.PlaceboEnergyStorage;

import java.util.function.Consumer;

public class ContainerUtil {

    public static void deserializeEnergy(PlaceboEnergyStorage energy, int value, boolean upper) {
        energy.setEnergy(merge(energy.getEnergy(), value, upper));
    }

    /**
     * IIntArray can only send shorts, so we need to split int values in two.
     *
     * @param value The int to split
     * @param upper If sending the upper bits or not.
     * @return The appropriate half of the integer.
     */
    public static int split(int value, boolean upper) {
        return upper ? value >> 16 : value & 0xFFFF;
    }

    /**
     * IIntArray can only send shorts, so we need to split int values in two.
     *
     * @param current The current value
     * @param value   The split integer, recieved from network
     * @param upper   If receiving the upper bits or not.
     * @return The updated value.
     */
    public static int merge(int current, int value, boolean upper) {
        if (upper) {
            return current & 0x0000FFFF | value << 16;
        } else {
            return current & 0xFFFF0000 | value & 0x0000FFFF;
        }
    }

    public static <T extends AbstractContainerMenu> MenuType<T> makeType(PosFactory<T> fac) {
        return new MenuType<>(factory(fac), FeatureFlags.REGISTRY.subset());
    }

    public static <T extends AbstractContainerMenu> PlaceboContainerFactory<T> factory(PosFactory<T> fac) {
        return (id, inv, buf) -> fac.create(id, inv, buf.readBlockPos());
    }

    public static <M extends AbstractContainerMenu> InteractionResult openGui(Player player, BlockPos pos, PosFactory<M> factory) {
        if (player.level().isClientSide) return InteractionResult.SUCCESS;
        openScreen((ServerPlayer) player, new SimplerMenuProvider<>(player.level(), pos, factory), buf -> buf.writeBlockPos(pos));
        return InteractionResult.CONSUME;
    }

    public static void openScreen(ServerPlayer player, MenuProvider containerSupplier, Consumer<FriendlyByteBuf> extraDataWriter) {
        player.doCloseContainer();
        player.nextContainerCounter();
        int openContainerId = player.containerCounter;
        FriendlyByteBuf extraData = new FriendlyByteBuf(Unpooled.buffer());
        extraDataWriter.accept(extraData);
        extraData.readerIndex(0); // reset to beginning in case modders read for whatever reason

        FriendlyByteBuf output = new FriendlyByteBuf(Unpooled.buffer());
        output.writeVarInt(extraData.readableBytes());
        output.writeBytes(extraData);

        if (output.readableBytes() > 32600 || output.readableBytes() < 1) {
            throw new IllegalArgumentException("Invalid PacketBuffer for openGui, found " + output.readableBytes() + " bytes");
        }
        AbstractContainerMenu c = containerSupplier.createMenu(openContainerId, player.getInventory(), player);
        MenuType<?> type = c.getType();
        var msg = new OpenContainer(type, openContainerId, containerSupplier.getDisplayName(), output);
        Placebo.CHANNEL.sendTo(msg, player);

        player.containerMenu = c;
        player.initMenu(player.containerMenu);
        Services.PLATFORM.containerOpened(player, c);
    }

    public interface PosFactory<T> {
        T create(int id, Inventory pInv, BlockPos pos);
    }

}
