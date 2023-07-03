package shadows.placebo.mixin.access;

import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TextColor.class)
public interface TextColorAccessor {
    @Mutable
    @Accessor("NAMED_COLORS")
    static void setNamedColors(Map<String, TextColor> colors) {
        throw null;
    }
}
