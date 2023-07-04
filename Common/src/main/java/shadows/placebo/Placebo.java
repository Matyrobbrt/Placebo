package shadows.placebo;

import io.github.matyrobbrt.eventdispatcher.EventBus;
import io.github.matyrobbrt.eventdispatcher.SubscribeEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadows.placebo.color.GradientColor;
import shadows.placebo.commands.PlaceboCommand;
import shadows.placebo.events.CommonSetupEvent;
import shadows.placebo.events.ModEventBus;
import shadows.placebo.events.PlayerTickEvent;
import shadows.placebo.events.RegisterCommandsEvent;
import shadows.placebo.mixin.access.TextColorAccessor;
import shadows.placebo.network.NetworkChannel;
import shadows.placebo.packets.ButtonClickMessage;
import shadows.placebo.packets.OpenContainer;
import shadows.placebo.packets.PatreonDisableMessage;
import shadows.placebo.packets.ReloadListenerPacket;
import shadows.placebo.platform.Services;
import shadows.placebo.util.PlaceboUtil;
import shadows.placebo.util.RegistryEvent.Register;

import java.util.HashMap;

public class Placebo {

	public static final String MODID = "placebo";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	//Formatter::off
    public static final NetworkChannel CHANNEL = Services.PLATFORM.createChannel(
			new ResourceLocation(MODID, MODID), Services.PLATFORM.getModVersion()
	);
	public static final EventBus BUS = EventBus.builder(MODID)
			.walksEventHierarcy(false).build();

	static {
		BUS.registerDispatcher(PlayerTickEvent.class, PlayerTickEvent.DISPATCHER);
	}

	private static final EventBus PLACEBO_BUS = ModEventBus.grabBus(MODID);
    //Formatter::on

	public static void init() {
		new Placebo();
	}

	public Placebo() {
		BUS.start();
		TextColorAccessor.setNamedColors(new HashMap<>(TextColor.NAMED_COLORS));
		BUS.register(Placebo.class);
		PLACEBO_BUS.register(this);
	}

	@SubscribeEvent
	public static void setup(CommonSetupEvent e) {
		CHANNEL.registerMessage(new ButtonClickMessage());
		CHANNEL.registerMessage(new PatreonDisableMessage(0));
		CHANNEL.registerMessage(new ReloadListenerPacket.Start(""));
		CHANNEL.registerMessage(new ReloadListenerPacket.Content<>("", null, null));
		CHANNEL.registerMessage(new ReloadListenerPacket.End(""));
		CHANNEL.registerMessage(new OpenContainer());
		e.enqueue(() -> PlaceboUtil.registerCustomColor(GradientColor.RAINBOW));
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent e) {
		PlaceboCommand.register(e.dispatcher());
	}

	@SubscribeEvent
	public void registerElse(Register<RecipeType<?>> event) {
		PlaceboUtil.registerTypes((resourceLocation, recipeType) -> event.getRegistry().register(recipeType, resourceLocation));
	}


}
