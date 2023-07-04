package shadows.placebo.forge.compat;

import com.google.auto.service.AutoService;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import shadows.placebo.compat.TrinketInventory;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Predicate;

@AutoService(TrinketInventory.Prober.class)
public class CuriosCompat implements TrinketInventory.Prober {

    @Override
    public boolean shallProbe() {
        return ModList.get().isLoaded("curios");
    }

    @Override
    public @Nullable TrinketInventory probe(LivingEntity entity) {
        return new TrinketInventory() {
            @Override
            public Optional<ItemStack> findFirstMatching(Predicate<ItemStack> predicate) {
                return CuriosApi.getCuriosHelper().findFirstCurio(entity, predicate).map(SlotResult::stack);
            }
        };
    }
}
