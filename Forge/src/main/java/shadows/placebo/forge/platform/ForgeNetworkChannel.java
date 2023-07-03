package shadows.placebo.forge.platform;

import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import shadows.placebo.network.MessageProvider;
import shadows.placebo.network.NetworkChannel;
import shadows.placebo.network.NetworkContext;

public class ForgeNetworkChannel implements NetworkChannel {
    private final SimpleChannel channel;
    private int id;

    public ForgeNetworkChannel(SimpleChannel channel) {
        this.channel = channel;
    }

    public ForgeNetworkChannel(ResourceLocation name, String version) {
        this.channel = NetworkRegistry.newSimpleChannel(
                name, () -> version, version::equals, version::equals
        );
    }

    @Override
    public <T> void registerMessage(MessageProvider<T> provider) {
        channel.messageBuilder(provider.getMsgClass(), id++)
                .decoder(provider::read)
                .encoder(provider::write)
                .consumerMainThread((t, contextSupplier) -> provider.handle(t, Suppliers.memoize(() -> new NetworkContext(contextSupplier.get().getSender(), contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER ? MessageProvider.Direction.C2S : MessageProvider.Direction.S2C))))
                .add();
    }

    @Override
    public void sendToServer(Object message) {
        channel.sendToServer(message);
    }

    @Override
    public void sendToAll(Object packet) {
        channel.send(PacketDistributor.ALL.with(() -> null), packet);
    }

    @Override
    public void sendTo(Object packet, Player player) {
        channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), packet);
    }

    @Override
    public void sendToTracking(Object packet, ServerLevel world, BlockPos pos) {
        world.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).forEach(p -> {
			channel.sendTo(packet, p.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		});
    }
}
