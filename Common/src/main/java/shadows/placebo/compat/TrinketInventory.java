package shadows.placebo.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Predicate;

public interface TrinketInventory {
    Optional<ItemStack> findFirstMatching(Predicate<ItemStack> predicate);

    default Optional<ItemStack> findFirstMatching(Item item) {
        return findFirstMatching(stack -> stack.getItem() == item);
    }

    static Optional<TrinketInventory> probe(LivingEntity wearer) {
        return Prober.PROBERS.stream()
                .map(prober -> prober.probe(wearer))
                .filter(Objects::nonNull)
                .findFirst();
    }

    interface Prober {
        List<Prober> PROBERS = ServiceLoader.load(Prober.class).stream().map(ServiceLoader.Provider::get).filter(Prober::shallProbe).toList();

        @Nullable TrinketInventory probe(LivingEntity entity);
        boolean shallProbe();
    }
}
