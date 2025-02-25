package shadows.placebo.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

public class JsonUtil {

	public static <T> T getRegistryObject(JsonObject parent, String name, Registry<T> registry) {
		String key = GsonHelper.getAsString(parent, name);
		T regObj = registry.get(new ResourceLocation(key));
		if (regObj == null) throw new JsonSyntaxException("Failed to parse " + registry.key() + " object with key " + key);
		return regObj;
	}

	public static <T> Object makeSerializer(Registry<T> reg) {
		return new SDS<>(reg);
	}

	/**
	 * Short for Serializer/Deserializer
	 */
	private static class SDS<T> implements com.google.gson.JsonDeserializer<T>, com.google.gson.JsonSerializer<T> {

		private final Registry<T> reg;

		SDS(Registry<T> reg) {
			this.reg = reg;
		}

		@Override
		public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(reg.getKey(src).toString());
		}

		@Override
		public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			T regObj = reg.get(new ResourceLocation(json.getAsString()));
			if (regObj == null) throw new JsonSyntaxException("Failed to parse " + reg.key() + " object with key " + json.getAsString());
			return regObj;
		}

	}

	public static interface JsonSerializer<V> {
		public JsonObject write(V src);
	}

	public static interface JsonDeserializer<V> {
		public V read(JsonObject json);
	}

	public static interface NetSerializer<V> {
		public void write(V src, FriendlyByteBuf buf);
	}

	public static interface NetDeserializer<V> {
		public V read(FriendlyByteBuf buf);
	}

	private static record SDS2<T>(com.google.gson.JsonDeserializer<T> jds, com.google.gson.JsonSerializer<T> js) implements com.google.gson.JsonDeserializer<T>, com.google.gson.JsonSerializer<T> {

		@Override
		public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
			return js.serialize(src, typeOfSrc, context);
		}

		@Override
		public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return jds.deserialize(json, typeOfT, context);
		}

	}

	public static <T> Object makeSerializer(com.google.gson.JsonDeserializer<T> jds, com.google.gson.JsonSerializer<T> js) {
		return new SDS2<>(jds, js);
	}

}
