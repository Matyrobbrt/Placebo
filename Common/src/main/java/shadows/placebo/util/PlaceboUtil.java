package shadows.placebo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import shadows.placebo.Placebo;
import shadows.placebo.recipe.RecipeHelper;

/**
 * Collection of misc util stuff.
 */
public class PlaceboUtil {

	/**
	 * Returns an ArrayList (non-fixed) with the provided elements.
	 */
	@SafeVarargs
	public static <T> List<T> asList(T... objs) {
		ArrayList<T> list = new ArrayList<>();
		for (T t : objs)
			list.add(t);
		return list;
	}

	public static CompoundTag getStackNBT(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null) stack.setTag(tag = new CompoundTag());
		return tag;
	}

	public static ItemStack[] toStackArray(Object... args) {
		ItemStack[] out = new ItemStack[args.length];
		for (int i = 0; i < args.length; i++)
			out[i] = RecipeHelper.makeStack(args[i]);
		return out;
	}

	/**
	 * Returns a mutable version of the passed list.
	 * If the list is already mutable, the list is returned.
	 */
	public static <T> List<T> toMutable(List<T> list) {
		if (list instanceof ImmutableList) {
			list = new ArrayList<T>(list);
		}
		return list;
	}

	/**
	 * Attempt to break a block as this player.
	 * @param player The player breaking the block.
	 * @param pos The location of the block to break.
	 * @return If a block was successfully broken.
	 */
	public static boolean tryHarvestBlock(ServerPlayer player, BlockPos pos) {
		return player.gameMode.destroyBlock(pos);
	}

	/**
	 * Adds a component to the lore tag of an itemstack/
	 * @param stack The ItemStack to append lore to.
	 * @param lore The actual lore.
	 */
	public static void addLore(ItemStack stack, Component lore) {
		CompoundTag display = stack.getOrCreateTagElement("display");
		ListTag tag = display.getList("Lore", 8);
		tag.add(StringTag.valueOf(Component.Serializer.toJson(lore)));
		display.put("Lore", tag);
	}

	static boolean late = false;
	static Map<ResourceLocation, RecipeType<?>> unregisteredTypes = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static <T extends Recipe<?>> RecipeType<T> makeRecipeType(final String pIdentifier) {
		if (late) throw new RuntimeException("Attempted to register a recipe type after the registration period closed.");
		RecipeType<T> type = new RecipeType<T>() {
			public String toString() {
				return pIdentifier;
			}
		};
		unregisteredTypes.put(new ResourceLocation(pIdentifier), type);
		return type;
	}

	public static void registerTypes(BiConsumer<ResourceLocation, RecipeType<?>> registrar) {
		unregisteredTypes.forEach(registrar);
		Placebo.LOGGER.debug("Registered {} recipe types.", unregisteredTypes.size());
		unregisteredTypes.clear();
		late = true;
	}

	/**
	 * Used to register a custom named color that extends TextColor.
	 * Should be called during common setup from within an enqueue work call.
	 * This is not required for any static color values, because they can be represented as a hex int.
	 */
	public static <T extends TextColor> void registerCustomColor(T color) {
		TextColor.NAMED_COLORS.put(color.serialize(), color);
	}

}
