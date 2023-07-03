package shadows.placebo.forge.platform;

import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import shadows.placebo.Constants;
import shadows.placebo.block_entity.TickingBlockEntityType;
import shadows.placebo.block_entity.TickingBlockEntity;
import shadows.placebo.network.NetworkChannel;
import shadows.placebo.platform.services.IPlatformHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public boolean isClient() {
        return FMLLoader.getDist().isClient();
    }

    @Override
    public Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    private final Supplier<String> modVersion = Suppliers.memoize(() -> ModList.get().getModContainerById(Constants.MOD_ID).orElseThrow().getModInfo().getVersion().toString());

    @Override
    public String getModVersion() {
        return modVersion.get();
    }

    @Override
    public <T extends BlockEntity & TickingBlockEntity> BlockEntityType<T> createTickingBEType(BlockEntitySupplier<T> supplier, Set<Block> blocks, boolean ticksOnClient, boolean ticksOnServer) {
        return new ForgeTickingBE<>(supplier::create, blocks, ticksOnClient, ticksOnServer);
    }

    @Override
    public NetworkChannel createChannel(ResourceLocation id, String version) {
        return new ForgeNetworkChannel(id, version);
    }

    @Override
    public void containerOpened(ServerPlayer player, AbstractContainerMenu containerMenu) {
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, containerMenu));
    }

    @Override
    public float getEnchantPower(BlockState state, Level world, BlockPos pos) {
        return state.getEnchantPowerBonus(world, pos);
    }

    private static final class ForgeTickingBE<T extends BlockEntity & TickingBlockEntity> extends BlockEntityType<T> implements TickingBlockEntityType<T> {
        private final boolean ticksOnClient, ticksOnServer;

        public ForgeTickingBE(BlockEntitySupplier<? extends T> blockEntitySupplier, Set<Block> set, boolean ticksOnClient, boolean ticksOnServer) {
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
