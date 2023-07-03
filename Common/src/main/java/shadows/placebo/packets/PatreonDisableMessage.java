package shadows.placebo.packets;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import shadows.placebo.Placebo;
import shadows.placebo.network.MessageProvider;
import shadows.placebo.network.NetworkContext;
import shadows.placebo.patreon.TrailsManager;
import shadows.placebo.patreon.WingsManager;

public class PatreonDisableMessage implements MessageProvider<PatreonDisableMessage> {

    private int type;
    private UUID id;

    public PatreonDisableMessage(int type) {
        this.type = type;
    }

    public PatreonDisableMessage(int type, UUID id) {
        this(type);
        this.id = id;
    }

    @Override
    public void write(PatreonDisableMessage msg, FriendlyByteBuf buf) {
        buf.writeByte(msg.type);
        buf.writeByte(msg.id == null ? 0 : 1);
        if (msg.id != null) buf.writeUUID(msg.id);
    }

    @Override
    public PatreonDisableMessage read(FriendlyByteBuf buf) {
        int type = buf.readByte();
        if (buf.readByte() == 1) {
            return new PatreonDisableMessage(type, buf.readUUID());
        } else return new PatreonDisableMessage(type);
    }

    @Override
    public void handle(PatreonDisableMessage msg, Supplier<NetworkContext> ctx) {
        if (ctx.get().direction() == Direction.C2S) {
            Placebo.CHANNEL.sendToAll(new PatreonDisableMessage(msg.type, ctx.get().player().getUUID()));
        } else {
            msg.handleClient(ctx.get());
        }
    }

    private void handleClient(NetworkContext context) {
        Set<UUID> set = type == 0 ? TrailsManager.DISABLED : WingsManager.DISABLED;
        if (set.contains(id)) {
            set.remove(id);
        } else set.add(id);
    }
}
