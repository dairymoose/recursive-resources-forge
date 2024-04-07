package nl.enjarai.recursiveresources.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;

public class SilentTexturedButtonWidget extends ImageButton {
    public SilentTexturedButtonWidget(int x, int y, int width, int height, int u, int v, ResourceLocation texture, Button.OnPress pressAction) {
        super(x, y, width, height, u, v, texture, pressAction);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        // Do nothing
    }
}
