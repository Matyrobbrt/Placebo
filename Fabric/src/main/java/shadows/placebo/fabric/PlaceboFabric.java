package shadows.placebo.fabric;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import shadows.placebo.Placebo;
import shadows.placebo.events.CommonSetupEvent;
import shadows.placebo.events.DatapackSyncEvent;
import shadows.placebo.events.RegisterCommandsEvent;
import shadows.placebo.events.RegisterReloadListenersEvent;
import shadows.placebo.json.ConditionHelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

public class PlaceboFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Placebo.BUS.post(new CommonSetupEvent(Runnable::run));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                Placebo.BUS.post(new RegisterCommandsEvent(dispatcher)));
        fireReloadListenerEvent(PackType.SERVER_DATA, (object, conditionsArrayName, logger) -> {
            try {
                JsonArray conditions = GsonHelper.getAsJsonArray(object, conditionsArrayName, null);

                if (conditions == null) {
                    return true; // no conditions
                } else {
                    return conditionsMatch(conditions, true);
                }
            } catch (RuntimeException exception) {
                logger.warn("Skipping object %s. Failed to parse resource conditions".formatted(object), exception);
                return false;
            }
        });
        fireReloadListenerEvent(PackType.CLIENT_RESOURCES, null);

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
                Placebo.BUS.post(new DatapackSyncEvent(player)));
    }

    private void fireReloadListenerEvent(PackType type, @Nullable ConditionHelper conditions) {
        final ResourceManagerHelper helper = ResourceManagerHelper.get(PackType.CLIENT_RESOURCES);
        Placebo.BUS.post(new RegisterReloadListenersEvent(
                type, conditions, (location, preparableReloadListener) -> helper.registerReloadListener(new IdentifiableResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return location;
                    }

                    @Override
                    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
                        return preparableReloadListener.reload(preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2);
                    }

                    @Override
                    public String getName() {
                        return preparableReloadListener.getName();
                    }
                })
        ));
    }

    /**
     * If {@code and} is true, check if all the passed conditions match.
     * If it is false, check if at least one of the passed conditions matches.
     *
     * @throws RuntimeException If some condition failed to parse.
     */
    public static boolean conditionsMatch(JsonArray conditions, boolean and) throws RuntimeException {
        for (JsonElement element : conditions) {
            if (element.isJsonObject()) {
                if (conditionMatches(element.getAsJsonObject()) != and) {
                    return !and;
                }
            } else {
                throw new JsonParseException("Invalid condition entry: " + element);
            }
        }

        return and;
    }

    /**
     * Check if the passed condition object matches.
     *
     * @throws RuntimeException If some condition failed to parse.
     */
    public static boolean conditionMatches(JsonObject condition) throws RuntimeException {
        final String type = condition.has("type") ? condition.get("type").getAsString() : GsonHelper.getAsString(condition, ResourceConditions.CONDITION_ID_KEY);
        final ResourceLocation conditionId = new ResourceLocation(type);
        final Predicate<JsonObject> jrc = ResourceConditions.get(conditionId);

        if (jrc == null) {
            throw new JsonParseException("Unknown recipe condition: " + conditionId);
        } else {
            return jrc.test(condition);
        }
    }
}
