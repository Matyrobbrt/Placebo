package shadows.placebo.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public interface NetworkChannel {
    <T> void registerMessage(MessageProvider<T> provider);

    void sendToServer(Object message);
    void sendTo(Object packet, Player player);
    void sendToTracking(Object packet, ServerLevel world, BlockPos pos);
    void sendToAll(Object packet);
}

