package shadows.placebo.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.TypeKeyed;
import shadows.placebo.network.MessageProvider;
import shadows.placebo.network.NetworkContext;

import java.util.function.Supplier;

public abstract class ReloadListenerPacket<T extends ReloadListenerPacket<T>> implements MessageProvider<T> {

	final String path;

	public ReloadListenerPacket(String path) {
		this.path = path;
	}

	public static class Start extends ReloadListenerPacket<Start> {

		public Start(String path) {
			super(path);
		}

		@Override
		public void write(Start msg, FriendlyByteBuf buf) {
			buf.writeUtf(msg.path, 50);
		}

		@Override
		public Start read(FriendlyByteBuf buf) {
			return new Start(buf.readUtf(50));
		}

		@Override
		public void handle(Start msg, Supplier<NetworkContext> ctx) {
			PlaceboJsonReloadListener.initSync(msg.path);
		}
	}

	public static class Content<V extends TypeKeyed<V>> extends ReloadListenerPacket<Content<V>> {

		final ResourceLocation key;
		final V item;

		public Content(String path, ResourceLocation key, V item) {
			super(path);
			this.key = key;
			this.item = item;
		}

		@Override
		public void write(Content<V> msg, FriendlyByteBuf buf) {
			buf.writeUtf(msg.path, 50);
			buf.writeResourceLocation(msg.key);
			PlaceboJsonReloadListener.writeItem(msg.path, msg.item, buf);
		}

		@Override
		public Content<V> read(FriendlyByteBuf buf) {
			String path = buf.readUtf(50);
			ResourceLocation key = buf.readResourceLocation();
			V item = PlaceboJsonReloadListener.readItem(path, key, buf);
			return new Content<>(path, key, item);
		}

		@Override
		public void handle(Content<V> msg, Supplier<NetworkContext> ctx) {
			PlaceboJsonReloadListener.acceptItem(msg.path, msg.item);
		}
	}

	public static class End extends ReloadListenerPacket<End> {

		public End(String path) {
			super(path);
		}

		@Override
		public void write(End msg, FriendlyByteBuf buf) {
			buf.writeUtf(msg.path, 50);
		}

		@Override
		public End read(FriendlyByteBuf buf) {
			return new End(buf.readUtf(50));
		}

		@Override
		public void handle(End msg, Supplier<NetworkContext> ctx) {
			PlaceboJsonReloadListener.endSync(msg.path);
		}
	}
}
