package shadows.placebo.events.client;

import io.github.matyrobbrt.eventdispatcher.Event;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface RegisterOverlaysEvent extends Event {
    void register(String id, GuiOverlay overlay);

    @FunctionalInterface
    interface GuiOverlay {
        void render(Gui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight);
    }
}
