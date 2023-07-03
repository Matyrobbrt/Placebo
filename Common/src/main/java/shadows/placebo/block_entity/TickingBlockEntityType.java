package shadows.placebo.block_entity;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface TickingBlockEntityType<T extends BlockEntity & TickingBlockEntity> {
    boolean ticksOnClient();

    boolean ticksOnServer();

    default BlockEntityTicker<T> getTicker(boolean client) {
        if (client && this.ticksOnClient()) return (level, pos, state, entity) -> entity.clientTick(level, pos, state);
        else if (!client && this.ticksOnServer()) return (level, pos, state, entity) -> entity.serverTick(level, pos, state);
        return null;
    }

}
