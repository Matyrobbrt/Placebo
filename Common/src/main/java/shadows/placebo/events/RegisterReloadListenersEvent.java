package shadows.placebo.events;

import io.github.matyrobbrt.eventdispatcher.Event;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.Nullable;
import shadows.placebo.json.ConditionHelper;

import java.util.function.BiConsumer;

public class RegisterReloadListenersEvent implements Event {
    private final PackType type;
    private final ConditionHelper conditionHelper;
    private final BiConsumer<ResourceLocation, PreparableReloadListener> registrar;

    public RegisterReloadListenersEvent(PackType type, @Nullable ConditionHelper conditionHelper, BiConsumer<ResourceLocation, PreparableReloadListener> registrar) {
        this.type = type;
        this.conditionHelper = conditionHelper == null ? ConditionHelper.NOOP : conditionHelper;
        this.registrar = registrar;
    }

    public void register(ResourceLocation id, PreparableReloadListener listener) {
        registrar.accept(id, listener);
    }

    public ConditionHelper getConditions() {
        return conditionHelper;
    }

    public PackType getType() {
        return type;
    }
}
