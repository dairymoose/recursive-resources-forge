package nl.enjarai.recursiveresources.mixin;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.packs.repository.FolderRepositorySource;

@Mixin(FolderRepositorySource.class)
public abstract class FolderRepositorySourceMixin {
    @Redirect(
            method = "getFactory",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"
            )
    )
    private static void recursiveresources$skipUnnessecaryLogging(Logger instance, String s, Object o) {
        // Don't log anything
    }
}
