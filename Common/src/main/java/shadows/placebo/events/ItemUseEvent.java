package shadows.placebo.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import io.github.matyrobbrt.eventdispatcher.Cancellable;
import io.github.matyrobbrt.eventdispatcher.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * This event is fired when an item would be used via {@link Item#useOn}<br>
 * It allows for the usage to be changed or cancelled.<br>
 * This event allows finer-tuned control over the actual item usage that cannot be achieved by using {@link RightClickBlock}
 */
public class ItemUseEvent implements Event, Cancellable {

	private final Player player;
	private final InteractionHand hand;
	private final BlockPos pos;
	@Nullable
	private final Direction face;
	private InteractionResult cancellationResult = InteractionResult.PASS;
	private final UseOnContext ctx;
	private boolean isCancelled;

	public ItemUseEvent(Player player, UseOnContext ctx) {
		this.player = player;
		this.hand = Preconditions.checkNotNull(ctx.getHand(), "Null hand in ItemUseEvent!");
		this.pos = Preconditions.checkNotNull(ctx.getClickedPos(), "Null position in ItemUseEvent!");
		this.face = ctx.getClickedFace();
		this.ctx = ctx;
	}

	public UseOnContext getContext() {
		return this.ctx;
	}

	@Nullable
	public Player getEntity() {
		return player;
	}

	/**
	 * @return The hand involved in this interaction. Will never be null.
	 */
	@Nonnull
	public InteractionHand getHand() {
		return hand;
	}

	/**
	 * @return The itemstack involved in this interaction, {@code ItemStack.EMPTY} if the hand was empty.
	 */
	@Nonnull
	public ItemStack getItemStack() {
		return ctx.getItemInHand();
	}

	/**
	 * If the interaction was on an entity, will be a BlockPos centered on the entity.
	 * If the interaction was on a block, will be the position of that block.
	 * Otherwise, will be a BlockPos centered on the player.
	 * Will never be null.
	 * @return The position involved in this interaction.
	 */
	@Nonnull
	public BlockPos getPos() {
		return pos;
	}

	/**
	 * @return The face involved in this interaction. For all non-block interactions, this will return null.
	 */
	@Nullable
	public Direction getFace() {
		return face;
	}

	/**
	 * @return Convenience method to get the world of this interaction.
	 */
	public Level getLevel() {
		return ctx.getLevel();
	}

	/**
	 * @return The InteractionResult that will be returned to vanilla if the event is cancelled, instead of calling the relevant
	 * method of the event. By default, this is {@link InteractionResult#PASS}, meaning cancelled events will cause
	 * the client to keep trying more interactions until something works.
	 */
	public InteractionResult getCancellationResult() {
		return cancellationResult;
	}

	/**
	 * Set the InteractionResult that will be returned to vanilla if the event is cancelled, instead of calling the relevant
	 * method of the event.
	 * Note that this only has an effect on {@link RightClickBlock}, {@link RightClickItem}, {@link EntityInteract}, and {@link EntityInteractSpecific}.
	 */
	public void setCancellationResult(InteractionResult result) {
		this.cancellationResult = result;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.isCancelled = cancelled;
	}
}
