package shadows.placebo.forge;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shadows.placebo.Placebo;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = Placebo.MODID)
public class PlaceboDatagen {
    @SubscribeEvent
    static void onDatagen(final GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), new ItemTagsProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()), Placebo.MODID, event.getExistingFileHelper()) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                tag("ingots/iron", "forge:ingots/iron", "c:iron_ingots", Items.IRON_INGOT);
                tag("ingots/netherite", "forge:ingots/netherite", "c:netherite_ingots", Items.NETHERITE_INGOT);
                tag("ingots/gold", "forge:ingots/gold", "c:gold_ingots", Items.GOLD_INGOT);

                tag("gems/diamond", "forge:gems/diamond", "c:diamonds", Items.DIAMOND);
                tag("gems/lapis", "forge:gems/lapis", "c:diamonds", Items.LAPIS_LAZULI);
                tag("obsidian", "forge:obsidian", Items.OBSIDIAN);
                tag("dusts/redstone", "forge:dusts/redstone", "c:redstone_dusts", Items.REDSTONE);
                tag("glass_panes", "forge:glass_panes", "c:lapis_lazulis")
                        .add(BuiltInRegistries.BLOCK.holders()
                                .filter(it -> it.key().location().getNamespace().equals("minecraft") && it.get() instanceof StainedGlassPaneBlock)
                                .map(it -> it.get().asItem())
                                .toArray(Item[]::new))
                        .add(Items.GLASS_PANE);
            }

            @CanIgnoreReturnValue
            private IntrinsicTagAppender<Item> tag(String name, Object... args) {
                final var tag = tag(TagKey.create(Registries.ITEM, new ResourceLocation(Placebo.MODID, name)));
                for (Object arg : args) {
                    if (arg instanceof Item it) {
                        tag.add(it);
                    } else {
                        tag.addOptionalTag(new ResourceLocation(arg.toString()));
                    }
                }
                return tag;
            }
        });
    }
}
