package shadows.placebo.fabric.comap;

import com.google.auto.service.AutoService;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import shadows.placebo.compat.TrinketInventory;

import java.util.Optional;
import java.util.function.Predicate;

@AutoService(TrinketInventory.Prober.class)
public class TrinketsCompat implements TrinketInventory.Prober {

    @Override
    public @Nullable TrinketInventory probe(LivingEntity entity) {
        return TrinketsApi.getTrinketComponent(entity)
                .map(component -> new TrinketInventory() {
                    @Override
                    public Optional<ItemStack> findFirstMatching(Predicate<ItemStack> predicate) {
                        final var equip = component.getEquipped(predicate);
                        return equip.isEmpty() ? Optional.empty() : Optional.of(equip.get(0).getB());
                    }
                })
                .orElse(null);
    }

    @Override
    public boolean shallProbe() {
        return FabricLoader.getInstance().getModContainer("trinkets").isPresent();
    }
}
