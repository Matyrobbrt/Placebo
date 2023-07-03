package shadows.placebo.network;

import net.minecraft.server.level.ServerPlayer;

public record NetworkContext(
        ServerPlayer player, MessageProvider.Direction direction
) {
}
