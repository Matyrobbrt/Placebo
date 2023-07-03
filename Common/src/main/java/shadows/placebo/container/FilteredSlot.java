package shadows.placebo.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class FilteredSlot extends Slot {
	protected final Predicate<ItemStack> filter;
	public FilteredSlot(Container handler, int index, int x, int y, Predicate<ItemStack> filter) {
		super(handler, index, x, y);
		this.filter = filter;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return this.filter.test(stack);
	}
}