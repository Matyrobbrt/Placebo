package shadows.placebo.fabric.platform;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import shadows.placebo.network.MessageProvider;
import shadows.placebo.network.NetworkChannel;
import shadows.placebo.network.NetworkContext;
import shadows.placebo.platform.Services;
import shadows.placebo.util.PlaceboTaskQueue;

public class FabricNetworkChannel implements NetworkChannel {
    private final ResourceLocation name;
    private int id;
    private final IdMapper<MessageProvider<?>> provides = new IdMapper<>();
    private final Object2IntMap<Class<?>> types = new Object2IntOpenHashMap<>();

    public FabricNetworkChannel(ResourceLocation name) {
        this.name = name;
        ServerPlayNetworking.registerGlobalReceiver(name, (server, player, handler, buf, responseSender) -> handle(buf, () -> new NetworkContext(player, MessageProvider.Direction.C2S)));
        if (Services.PLATFORM.isClient()) {
            ClientPlayNetworking.registerGlobalReceiver(name, (client, handler, buf, responseSender) -> handle(buf, () -> new NetworkContext(null, MessageProvider.Direction.S2C)));
        }
    }

    @SuppressWarnings("all")
    private void handle(FriendlyByteBuf buf, Supplier<NetworkContext> contextSupplier) {
        final int id = buf.readInt();
        final MessageProvider provider = provides.byIdOrThrow(id);
        provider.handle(provider.read(buf), Suppliers.memoize(contextSupplier));
    }

    @Override
    public <T> void registerMessage(MessageProvider<T> provider) {
        final int newId = id++;
        provides.addMapping(provider, newId);
        types.put(provider.getMsgClass(), newId);
    }

    @Override
    public void sendToServer(Object message) {
        ClientPlayNetworking.send(name, writeMessage(message));
    }

    @Override
    public void sendToAll(Object packet) {
        PlaceboTaskQueue.currentServer.getPlayerList().broadcastAll(
                ServerPlayNetworking.createS2CPacket(name, writeMessage(packet))
        );
    }

    @Override
    public void sendTo(Object packet, Player player) {
        ServerPlayNetworking.send((ServerPlayer) player, name, writeMessage(packet));
    }

    private FriendlyByteBuf writeMessage(Object message) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        final int packetId = types.getInt(message.getClass());
        buf.writeInt(packetId);
        ((MessageProvider) provides.byId(packetId)).write(message, buf);
        return buf;
    }

    @Override
    public void sendToTracking(Object packet, ServerLevel world, BlockPos pos) {
        world.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).forEach(p -> sendTo(packet, p));
    }
}
