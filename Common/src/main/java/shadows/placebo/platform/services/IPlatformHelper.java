package shadows.placebo.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import shadows.placebo.block_entity.TickingBlockEntity;
import shadows.placebo.network.NetworkChannel;
import shadows.placebo.platform.PlatformType;

import java.nio.file.Path;
import java.util.Set;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    boolean isClient();

    Path getGameDir();
    Path getConfigDir();

    String getModVersion();

    PlatformType getPlatform();

    <T extends BlockEntity & TickingBlockEntity> BlockEntityType<T> createTickingBEType(BlockEntitySupplier<T> supplier, Set<Block> blocks, boolean ticksOnClient, boolean ticksOnServer);

    void containerOpened(ServerPlayer player, AbstractContainerMenu containerMenu);

    NetworkChannel createChannel(ResourceLocation id, String version);

    float getEnchantPower(BlockState state, Level world, BlockPos pos);

    @FunctionalInterface
    interface BlockEntitySupplier<T extends BlockEntity & TickingBlockEntity> {
        T create(BlockPos var1, BlockState var2);
    }
}
