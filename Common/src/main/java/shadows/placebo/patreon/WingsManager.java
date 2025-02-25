package shadows.placebo.patreon;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import shadows.placebo.Placebo;
import shadows.placebo.events.client.ClientSetupEvent;
import shadows.placebo.events.client.ClientTickEvent;
import shadows.placebo.events.client.RegisterLayerDefinitionsEvent;
import shadows.placebo.events.client.RegisterLayersEvent;
import shadows.placebo.mixin.access.LivingEntityRendererAccessor;
import shadows.placebo.packets.PatreonDisableMessage;
import shadows.placebo.patreon.PatreonUtils.WingType;
import shadows.placebo.patreon.wings.Wing;
import shadows.placebo.patreon.wings.WingLayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WingsManager {

	static Map<UUID, WingType> WINGS = new HashMap<>();
	public static final KeyMapping TOGGLE = new KeyMapping("placebo.toggleWings", GLFW.GLFW_KEY_KP_8, "key.categories.placebo");
	public static final Set<UUID> DISABLED = new HashSet<>();
	public static final ModelLayerLocation WING_LOC = new ModelLayerLocation(new ResourceLocation(Placebo.MODID, "wings"), "main");

	public static void init(ClientSetupEvent e) {
		Placebo.BUS.addListener((final RegisterLayerDefinitionsEvent event) -> event.register(WING_LOC, Wing::createLayer));
		Placebo.BUS.addListener(WingsManager::addLayers);
		new Thread(() -> {
			Placebo.LOGGER.info("Loading patreon wing data...");
			try {
				URL url = new URL("https://raw.githubusercontent.com/Shadows-of-Fire/Placebo/1.16/PatreonWings.txt");
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
					String s;
					while ((s = reader.readLine()) != null) {
						String[] split = s.split(" ", 2);
						if (split.length != 2) {
							Placebo.LOGGER.error("Invalid patreon wing entry {} will be ignored.", s);
							continue;
						}
						WINGS.put(UUID.fromString(split[0]), WingType.valueOf(split[1]));
					}
				} catch (IOException ex) {
					Placebo.LOGGER.error("Exception loading patreon wing data!");
					ex.printStackTrace();
				}
			} catch (Exception k) {
				//not possible
			}
			Placebo.LOGGER.info("Loaded {} patreon wings.", WINGS.size());
			if (WINGS.size() > 0) Placebo.BUS.addListener(WingsManager::clientTick);
		}, "Placebo Patreon Wing Loader").start();
	}

	public static void clientTick(ClientTickEvent e) {
		if (TOGGLE.consumeClick()) Placebo.CHANNEL.sendToServer(new PatreonDisableMessage(1));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addLayers(RegisterLayersEvent e) {
		Wing.INSTANCE = new Wing(e.getEntityModels().bakeLayer(WING_LOC));
		for (String s : e.getSkins()) {
			LivingEntityRenderer skin = e.getSkin(s);
			((LivingEntityRendererAccessor) skin).placebo$addLayer(new WingLayer(skin));
		}
	}

	public static WingType getType(UUID id) {
		return WINGS.get(id);
	}

}
