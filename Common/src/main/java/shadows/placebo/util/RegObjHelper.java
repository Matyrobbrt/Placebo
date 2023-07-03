package shadows.placebo.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.material.Fluid;
import shadows.placebo.reg.RegistryObject;

public class RegObjHelper {

	protected final String modid;

	public RegObjHelper(String modid) {
		this.modid = modid;
	}

	public <T extends Block> RegistryObject<T> block(String path) {
		return create(modid, path, Registry.BLOCK);
	}

	public <T extends Fluid> RegistryObject<T> fluid(String path) {
		return create(modid, path, Registry.FLUID);
	}

	public <T extends Item> RegistryObject<T> item(String path) {
		return create(modid, path, Registry.ITEM);
	}

	public <T extends MobEffect> RegistryObject<T> effect(String path) {
		return create(modid, path, Registry.MOB_EFFECT);
	}

	public <T extends SoundEvent> RegistryObject<T> sound(String path) {
		return create(modid, path, Registry.SOUND_EVENT);
	}

	public <T extends Potion> RegistryObject<T> potion(String path) {
		return create(modid, path, Registry.POTION);
	}

	public <T extends Enchantment> RegistryObject<T> enchant(String path) {
		return create(modid, path, Registry.ENCHANTMENT);
	}

	public <U extends Entity, T extends EntityType<U>> RegistryObject<T> entity(String path) {
		return create(modid, path, Registry.ENTITY_TYPE);
	}

	public <U extends BlockEntity, T extends BlockEntityType<U>> RegistryObject<T> blockEntity(String path) {
		return create(modid, path, Registry.BLOCK_ENTITY_TYPE);
	}

	public <U extends ParticleOptions, T extends ParticleType<U>> RegistryObject<T> particle(String path) {
		return create(modid, path, Registry.PARTICLE_TYPE);
	}

	public <U extends AbstractContainerMenu, T extends MenuType<U>> RegistryObject<T> menu(String path) {
		return create(modid, path, Registry.MENU);
	}

	public <T extends PaintingVariant> RegistryObject<T> painting(String path) {
		return create(modid, path, Registry.PAINTING_VARIANT);
	}

	public <C extends Container, U extends Recipe<C>, T extends RecipeType<U>> RegistryObject<T> recipe(String path) {
		return create(modid, path, Registry.RECIPE_TYPE);
	}

	public <C extends Container, U extends Recipe<C>, T extends RecipeSerializer<U>> RegistryObject<T> recipeSerializer(String path) {
		return create(modid, path, Registry.RECIPE_SERIALIZER);
	}

	public <T extends Attribute> RegistryObject<T> attribute(String path) {
		return create(modid, path, Registry.ATTRIBUTE);
	}

	public <S, U extends Stat<S>, T extends StatType<U>> RegistryObject<T> stat(String path) {
		return create(modid, path, Registry.STAT_TYPE);
	}

	public <U extends FeatureConfiguration, T extends Feature<U>> RegistryObject<T> feature(String path) {
		return create(modid, path, Registry.FEATURE);
	}

	public <T> RegistryObject<T> custom(String path, Registry<T> registry) {
		return create(modid, path, registry);
	}

	public static <T> RegistryObject<T> create(String modid, String path, Registry<? super T> registry) {
		return new RegistryObject<>() {
			final ResourceKey<T> key = (ResourceKey<T>) ResourceKey.create(registry.key(), new ResourceLocation(modid, path));
			@Override
			public ResourceKey<T> getResourceKey() {
				return key;
			}

			@Override
			public ResourceLocation getId() {
				return key.location();
			}

			private T value;
			@Override
			public T get() {
				if (value == null) {
					return value = (T) registry.get(key.location());
				}
				return value;
			}

			private Holder<T> holder;
			@Override
			public Holder<T> asHolder() {
				if (holder == null) {
					return holder = registry.getHolderOrThrow((ResourceKey)key);
				}
				return holder;
			}
		};
	}

}
