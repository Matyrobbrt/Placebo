package shadows.placebo.events;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import shadows.placebo.Placebo;

public class PlaceboEventFactory {

	public static InteractionResult onItemUse(ItemStack stack, UseOnContext ctx) {
		ItemUseEvent event = new ItemUseEvent(ctx.getPlayer(), ctx);
		Placebo.BUS.post(event);
		if (event.isCancelled()) return event.getCancellationResult();
		return null;
	}
}
