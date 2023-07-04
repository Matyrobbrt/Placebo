package shadows.placebo.util;

import com.mojang.datafixers.util.Pair;
import io.github.matyrobbrt.eventdispatcher.Event;
import io.github.matyrobbrt.eventdispatcher.GenericEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.datafixers.util.Pair.of;

public class RegistryEvent<T> implements Event, GenericEvent<T> {

	public static final Map<ResourceKey<? extends Registry<?>>, Class<?>> REGISTRIES_TO_FIRE = Stream.of(
			of(Block.class, Registries.BLOCK),
			of(Fluid.class, Registries.FLUID),
			of(Item.class, Registries.ITEM),
			of(MobEffect.class, Registries.MOB_EFFECT),
			of(SoundEvent.class, Registries.SOUND_EVENT),
			of(Potion.class, Registries.POTION),
			of(Enchantment.class, Registries.ENCHANTMENT),
			of(EntityType.class, Registries.ENTITY_TYPE),
			of(BlockEntityType.class, Registries.BLOCK_ENTITY_TYPE),
			of(ParticleType.class, Registries.PARTICLE_TYPE),
			of(MenuType.class, Registries.MENU),
			of(PaintingVariant.class, Registries.PAINTING_VARIANT),
			of(RecipeType.class, Registries.RECIPE_TYPE),
			of(RecipeSerializer.class, Registries.RECIPE_SERIALIZER),
			of(Attribute.class, Registries.ATTRIBUTE),
			of(StatType.class, Registries.STAT_TYPE),
			of(CreativeModeTab.class, Registries.CREATIVE_MODE_TAB)
	).collect(Collectors.toMap(Pair::getSecond, Pair::getFirst, (aClass, aClass2) -> aClass2, LinkedHashMap::new));

	private final Class<T> clazz;
	RegistryEvent(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Type getGenericType() {
		return clazz;
	}

	/**
	 * Generic-type registry event reminiscent of days long past.
	 * This is fired for the same set of registries that {@link RegObjHelper} has helpers for.
	 */
	public static class Register<T> extends RegistryEvent<T> {
		private final Registry<T> registry;
		private final ResourceLocation name;
		private final RegistryWrapper<T> wrapper;

		public Register(Class<T> clazz, Registry<T> registry, RegistryWrapper<T> wrapper) {
			super(clazz);
			this.name = registry.key().location();
			this.registry = registry;
			this.wrapper = wrapper;
		}

		/**
		 * Gets a registry wrapper that has a registerAll helper.
		 */
		public RegistryWrapper<T> getRegistry() {
			return wrapper;
		}

		public Registry<T> getVanillaRegistry() {
			return registry;
		}

		public ResourceLocation getName() {
			return name;
		}

		@Override
		public String toString() {
			return "RegistryEvent.Register<" + getName() + ">";
		}
	}

	public interface RegistryWrapper<T> {
		/**
		 * Registers a single object.
		 * @param object The object being registered
		 * @param id The ID of the object being registered. A modid will be filled in from context if absent.
		 */
		void register(T object, String id);

		/**
		 * Registers a single object.
		 * @param object The object being registered
		 * @param id The ID of the object being registered.
		 */
		void register(T object, ResourceLocation id);

		/**
		 * Registers multiple objects. Objects must be passed [object, id] repeating.
		 * This method does not validate that an ID is present, and will crash if one is not.
		 * @param arr The vararg array of objects and ids.
		 */
		@SuppressWarnings("unchecked")
		default void registerAll(Object... arr) {
			for (int i = 0; i < arr.length; i += 2) {
				T object = (T) arr[i];
				Object id = arr[i + 1];
				if (id instanceof String s) register(object, s);
				else if (id instanceof ResourceLocation r) register(object, r);
				else throw new RuntimeException();
			}
		}
	}
}