package nl.enjarai.recursiveresources.mixin;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.network.chat.Component;
import nl.enjarai.recursiveresources.gui.FolderedTransferableSelectionList;

@Mixin(TransferableSelectionList.class)
public abstract class TransferableSelectionListMixin extends AbstractSelectionListMixin implements FolderedTransferableSelectionList {
    @Shadow @Final PackSelectionScreen screen;
    @Unique
    @Nullable
    private Component titleHoverText;
    @Unique
    @Nullable
    private Component titleTooltip;
    @Unique
    @Nullable
    private Runnable titleClickEvent;

    @Override
    public void recursiveresources$setTitleClickable(Component hoverText, Component tooltip, Runnable clickEvent) {
        this.titleHoverText = hoverText;
        this.titleTooltip = tooltip;
        this.titleClickEvent = clickEvent;
    }

    @Override
    protected boolean recursiveresources$modifyHeaderRendering(AbstractSelectionList<?> thiz, GuiGraphics context, int x, int y,
                                                               @Share("mouseX") LocalIntRef mouseXRef, @Share("mouseY") LocalIntRef mouseYRef) {
        if (titleHoverText != null) {
            Component text = Component.empty().append(titleHoverText).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD, ChatFormatting.ITALIC);
            int textWidth = minecraft.font.width(text);

            int left = x + width / 2 - textWidth / 2;
            int top = Math.min(this.height + 3, y);
            int right = left + textWidth;
            int bottom = top + minecraft.font.lineHeight;

            int mouseX = mouseXRef.get();
            int mouseY = mouseYRef.get();

            if (mouseX >= x && mouseX <= x + width && mouseY >= top && mouseY <= bottom) {
                context.drawString(minecraft.font, text, left, top, 16777215, false);

                if (titleTooltip != null) {
                    screen.setTooltipForNextRenderPass(List.of(titleTooltip.getVisualOrderText()));
                }

                return false;
            }
        }

        return true;
    }

    @Override
    protected void recursiveresources$handleHeaderClick(int x, int y, CallbackInfo ci) {
        if (titleClickEvent != null && y <= minecraft.font.lineHeight) { //client.render.fontHeight
            titleClickEvent.run();
        }
    }
}
