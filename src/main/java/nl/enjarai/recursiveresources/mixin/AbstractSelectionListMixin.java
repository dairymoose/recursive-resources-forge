package nl.enjarai.recursiveresources.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;

@Mixin(AbstractSelectionList.class)
public abstract class AbstractSelectionListMixin {
    @Shadow @Final protected Minecraft minecraft;

    @Shadow protected int width;

    @Shadow protected int height;  //top?

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void recursiveresources$captureParams(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci,
                                                  @Share("mouseX") LocalIntRef mouseXRef, @Share("mouseY") LocalIntRef mouseYRef) {
        mouseXRef.set(mouseX);
        mouseYRef.set(mouseY);
    }

    @WrapWithCondition(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/AbstractSelectionList;renderHeader(Lnet/minecraft/client/gui/GuiGraphics;II)V"
            )
    )
    protected boolean recursiveresources$modifyHeaderRendering(AbstractSelectionList<?> thiz, GuiGraphics context, int x, int y,
                                                               @Share("mouseX") LocalIntRef mouseXRef, @Share("mouseY") LocalIntRef mouseYRef) {
        return true;
    }

    @Inject(
            method = "clickedHeader",
            at = @At("HEAD")
    )
    protected void recursiveresources$handleHeaderClick(int x, int y, CallbackInfo ci) {
    }
}
