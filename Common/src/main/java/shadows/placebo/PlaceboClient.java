package shadows.placebo;

import io.github.matyrobbrt.eventdispatcher.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextColor;
import shadows.placebo.color.GradientColor;
import shadows.placebo.events.client.ClientSetupEvent;
import shadows.placebo.events.client.ClientTickEvent;
import shadows.placebo.events.client.RegisterKeysEvent;
import shadows.placebo.patreon.PatreonPreview;
import shadows.placebo.patreon.TrailsManager;
import shadows.placebo.patreon.WingsManager;
import shadows.placebo.util.PlaceboUtil;

public class PlaceboClient {

	public static void init() {
		Placebo.BUS.registerDispatcher(ClientTickEvent.class, ClientTickEvent.DISPATCHER);
		Placebo.BUS.register(PatreonPreview.class);
	}

	@SubscribeEvent
	public static void setup(ClientSetupEvent e) {
		TrailsManager.init();
		WingsManager.init(e);
	}

	@SubscribeEvent
	public static void keys(RegisterKeysEvent e) {
		e.register(TrailsManager.TOGGLE);
		e.register(WingsManager.TOGGLE);
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent e) {
		if (e.phase() == ClientTickEvent.Phase.END) {
			ticks++;
		}
	}

	/**
	 * @see PlaceboUtil#registerCustomColor(String, TextColor)
	 */
	@Deprecated(forRemoval = true)
	public static <T extends TextColor> void registerCustomColor(String id, T color) {
		PlaceboUtil.registerCustomColor(color);
	}

	public static long ticks = 0;

	public static float getColorTicks() {
		return (ticks + Minecraft.getInstance().getDeltaFrameTime()) / 0.5F;
	}

	@Deprecated(forRemoval = true)
	public static class RainbowColor extends GradientColor {

		public RainbowColor() {
			super(GradientColor.RAINBOW_GRADIENT, "rainbow");
		}
	}
}