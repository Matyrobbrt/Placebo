package shadows.placebo.events.client;

import io.github.matyrobbrt.eventdispatcher.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Function;

public final class RegisterLayersEvent implements Event {
    private final Function<EntityType<?>, EntityRenderer<?>> renderers;
    private final Function<String, EntityRenderer<? extends Player>> skinGetter;
    private final Set<String> skins;
    private final EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();

    @ApiStatus.Internal
    public RegisterLayersEvent(Function<EntityType<?>, EntityRenderer<?>> renderers, Function<String, EntityRenderer<? extends Player>> skinGetter, Set<String> skins) {
        this.renderers = renderers;
        this.skinGetter = skinGetter;
        this.skins = skins;
    }

    /**
     * {@return the set of player skin names which have a renderer}
     * <p>
     * Minecraft provides two default skin names: {@code default} for the
     * {@linkplain ModelLayers#PLAYER regular player model} and {@code slim} for the
     * {@linkplain ModelLayers#PLAYER_SLIM slim player model}.
     */
    public Set<String> getSkins() {
        return skins;
    }

    /**
     * Returns a player skin renderer for the given skin name.
     *
     * @param skinName the name of the skin to get the renderer for
     * @param <R>      the type of the skin renderer, usually {@link PlayerRenderer}
     * @return the skin renderer, or {@code null} if no renderer is registered for that skin name
     * @see #getSkins()
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <R extends LivingEntityRenderer<? extends Player, ? extends EntityModel<? extends Player>>> R getSkin(String skinName) {
        return (R) skinGetter.apply(skinName);
    }

    /**
     * Returns an entity renderer for the given entity type.
     *
     * @param entityType the entity type to return a renderer for
     * @param <T>        the type of entity the renderer is for
     * @param <R>        the type of the renderer
     * @return the renderer, or {@code null} if no renderer is registered for that entity type
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends LivingEntity, R extends LivingEntityRenderer<T, ? extends EntityModel<T>>> R getRenderer(EntityType<? extends T> entityType) {
        return (R) renderers.apply(entityType);
    }

    /**
     * {@return the set of entity models}
     */
    public EntityModelSet getEntityModels() {
        return entityModels;
    }
}
