package shadows.placebo.fabric.platform;

import com.google.common.base.Suppliers;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import shadows.placebo.Constants;
import shadows.placebo.block_entity.TickingBlockEntity;
import shadows.placebo.block_entity.TickingBlockEntityType;
import shadows.placebo.network.NetworkChannel;
import shadows.placebo.platform.PlatformType;
import shadows.placebo.platform.services.IPlatformHelper;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public PlatformType getPlatform() {
        return PlatformType.FABRIC;
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    private final Supplier<String> modVersion = Suppliers.memoize(() ->
            FabricLoader.getInstance().getModContainer(Constants.MOD_ID).orElseThrow().getMetadata().getVersion().toString());
    @Override
    public String getModVersion() {
        return modVersion.get();
    }

    @Override
    public <T extends BlockEntity & TickingBlockEntity> BlockEntityType<T> createTickingBEType(BlockEntitySupplier<T> supplier, Set<Block> blocks, boolean ticksOnClient, boolean ticksOnServer) {
        return new FabricTickingBE<>(supplier::create, blocks, ticksOnClient, ticksOnServer);
    }

    @Override
    public void containerOpened(ServerPlayer player, AbstractContainerMenu containerMenu) {

    }

    @Override
    public float getEnchantPower(BlockState state, Level world, BlockPos pos) {
        return state.is(Blocks.BOOKSHELF) ? 1 : 0;
    }

    @Override
    public NetworkChannel createChannel(ResourceLocation id, String version) {
        return new FabricNetworkChannel(id);
    }

    private static final class FabricTickingBE<T extends BlockEntity & TickingBlockEntity> extends BlockEntityType<T> implements TickingBlockEntityType<T> {
        private final boolean ticksOnClient, ticksOnServer;

        public FabricTickingBE(BlockEntitySupplier<? extends T> blockEntitySupplier, Set<Block> set, boolean ticksOnClient, boolean ticksOnServer) {
            super(blockEntitySupplier, set, null);
            this.ticksOnClient = ticksOnClient;
            this.ticksOnServer = ticksOnServer;
        }

        @Override
        public boolean ticksOnClient() {
            return ticksOnClient;
        }

        @Override
        public boolean ticksOnServer() {
            return ticksOnServer;
        }
    }
}
