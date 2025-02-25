package shadows.placebo.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import shadows.placebo.Placebo;
import shadows.placebo.codec.EnumCodec;
import shadows.placebo.util.StepFunction;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class RandomAttributeModifier {

	//Formatter::off
	public static Codec<RandomAttributeModifier> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			BuiltInRegistries.ATTRIBUTE.byNameCodec().fieldOf("attribute").forGetter(a -> a.attribute),
			new EnumCodec<>(Operation.class).fieldOf("operation").forGetter(a -> a.op),
			StepFunction.CODEC.fieldOf("value").forGetter(a -> a.value))
			.apply(inst, RandomAttributeModifier::new)
		);
	//Formatter::on

	protected final Attribute attribute;
	protected final Operation op;
	protected final StepFunction value;
	protected final UUID id;

	/**
	 * Creates a Chanced Effect Instance.
	 * @param chance The chance this potion is received.
	 * @param effect The effect.
	 * @param amp A random range of possible amplifiers.
	 */
	public RandomAttributeModifier(Attribute attribute, Operation op, StepFunction value) {
		this.attribute = attribute;
		this.op = op;
		this.value = value;
		Random rand = new Random();
		rand.setSeed(Objects.hash(attribute, op, value));
		this.id = new UUID(rand.nextLong(), rand.nextLong());
	}

	public void apply(RandomSource rand, LivingEntity entity) {
		if (entity == null) throw new RuntimeException("Attempted to apply a random attribute modifier to a null entity!");
		AttributeModifier modif = genModifier(rand);
		AttributeInstance inst = entity.getAttribute(this.attribute);
		if (inst == null) {
			Placebo.LOGGER.trace(String.format("Attempted to apply a random attribute modifier to an entity (%s) that does not have that attribute (%s)!", EntityType.getKey(entity.getType()), BuiltInRegistries.ATTRIBUTE.getKey(this.attribute)));
			return;
		}
		inst.addPermanentModifier(modif);
	}

	public AttributeModifier genModifier(RandomSource rand) {
		return new AttributeModifier(this.id, "placebo_random_modifier_" + this.attribute.getDescriptionId(), this.value.get(rand.nextFloat()), this.op);
	}

	public AttributeModifier genModifier(String name, RandomSource rand) {
		return new AttributeModifier(name, this.value.get(rand.nextFloat()), this.op);
	}

	public Attribute getAttribute() {
		return this.attribute;
	}

	public Operation getOp() {
		return this.op;
	}

	public StepFunction getValue() {
		return this.value;
	}

	public static class Deserializer implements JsonDeserializer<RandomAttributeModifier>, JsonSerializer<RandomAttributeModifier> {

		@Override
		public RandomAttributeModifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			String _attribute = obj.get("attribute").getAsString();
			Operation op = ctx.deserialize(obj.get("operation"), Operation.class);
			StepFunction value;
			if (obj.get("value").isJsonObject()) {
				JsonObject valueObj = GsonHelper.getAsJsonObject(obj, "value");
				value = ctx.deserialize(valueObj, StepFunction.class);
			} else {
				float v = GsonHelper.getAsFloat(obj, "value");
				value = new StepFunction(v, 1, 0);
			}
			Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(new ResourceLocation(_attribute));
			if (attribute == null || value == null || op == null) throw new JsonParseException("Attempted to deserialize invalid RandomAttributeModifier: " + json.toString());
			return new RandomAttributeModifier(attribute, op, value);
		}

		@Override
		public JsonElement serialize(RandomAttributeModifier src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			obj.addProperty("attribute", BuiltInRegistries.ATTRIBUTE.getKey(src.attribute).toString());
			obj.addProperty("operation", src.op.name());
			StepFunction range = src.value;
			if (range.min() == range.max()) {
				obj.addProperty("value", range.min());
			} else {
				obj.add("value", context.serialize(src.value));
			}
			return obj;
		}
	}
}