package shadows.placebo.events;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import shadows.placebo.Placebo;

import java.util.HashMap;
import java.util.Map;

public class PlaceboEventFactory {

	public static InteractionResult onItemUse(ItemStack stack, UseOnContext ctx) {
		ItemUseEvent event = new ItemUseEvent(ctx.getPlayer(), ctx);
		Placebo.BUS.post(event);
		if (event.isCancelled()) return event.getCancellationResult();
		return null;
	}

	/**
	 * Called from {@link IForgeItemStack#getEnchantmentLevel(Enchantment)}
	 * 
	 * Injected via coremods/get_ench_level_event_specific.js
	 */
	public static int getEnchantmentLevelSpecific(int level, ItemStack stack, Enchantment ench) {
		var map = new HashMap<Enchantment, Integer>();
		map.put(ench, level);
		var event = new GetEnchantmentLevelEvent(stack, map);
		Placebo.BUS.post(event);
		return event.getEnchantments().get(ench);
	}

	/**
	 * Called from {@link IForgeItemStack#getAllEnchantments()}
	 * 
	 * Injected via coremods/get_ench_level_event.js
	 */
	public static Map<Enchantment, Integer> getEnchantmentLevel(Map<Enchantment, Integer> enchantments, ItemStack stack) {
		enchantments = new HashMap<>(enchantments);
		var event = new GetEnchantmentLevelEvent(stack, enchantments);
		Placebo.BUS.post(event);
		return enchantments;
	}
}
