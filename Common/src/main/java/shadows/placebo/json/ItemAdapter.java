package shadows.placebo.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class ItemAdapter implements JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {

	public static final ItemAdapter INSTANCE = new ItemAdapter();

	public static final Gson ITEM_READER = new GsonBuilder().registerTypeAdapter(ItemStack.class, INSTANCE).registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE).registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();

	//Formatter::off
	public static final Codec<ItemStack> CODEC = RecordCodecBuilder.create(inst -> inst
			.group(
					BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ItemStack::getItem),
					Codec.intRange(0, 64).optionalFieldOf("count", 1).forGetter(ItemStack::getCount),
					CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(stack -> Optional.ofNullable(stack.getTag())))
			.apply(inst, (item, count, nbt) -> {
				var stack = new ItemStack(item, count);
				nbt.ifPresent(stack::setTag);
				return stack;
			})
	);
	//Formatter::on


	@Override
	public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		ResourceLocation id = ctx.deserialize(obj.get("item"), ResourceLocation.class);
		Item item = BuiltInRegistries.ITEM.get(id);
		boolean optional = obj.has("optional") ? obj.get("optional").getAsBoolean() : false;
		if (!optional && item == Items.AIR && !id.equals(BuiltInRegistries.ITEM.getKey(Items.AIR))) throw new JsonParseException("Failed to read non-optional item " + id);
		int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
		CompoundTag tag = null;
		if (obj.has("nbt")) {
			var nbt = obj.get("nbt");
			if (nbt.isJsonObject()) {
				tag = CompoundTag.CODEC.decode(JsonOps.INSTANCE, nbt).get().orThrow().getFirst();
			} else {
				tag = ctx.deserialize(obj.get("nbt"), CompoundTag.class);
			}
		}
		ItemStack stack = new ItemStack(item, count);
		stack.setTag(tag);
		return stack;
	}

	@Override
	public JsonElement serialize(ItemStack stack, Type typeOfSrc, JsonSerializationContext ctx) {
		JsonObject obj = new JsonObject();
		obj.add("item", ctx.serialize(BuiltInRegistries.ITEM.getKey(stack.getItem())));
		obj.add("count", ctx.serialize(stack.getCount()));
		if (stack.hasTag()) obj.add("nbt", ctx.serialize(stack.getTag()));
		return obj;
	}

	public static ItemStack readStack(JsonElement obj) {
		return ITEM_READER.fromJson(obj, ItemStack.class);
	}

	public static List<ItemStack> readStacks(JsonElement obj) {
		return ITEM_READER.fromJson(obj, new TypeToken<List<ItemStack>>() {
		}.getType());
	}

}