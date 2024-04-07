package nl.enjarai.recursiveresources.mixin;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import nl.enjarai.recursiveresources.pack.NestedFolderPackProvider;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Inject(
            method = "<init>([Lnet/minecraft/server/packs/repository/RepositorySource;)V",
            at = @At("RETURN")
    )
    private void recursiveresources$onInit(CallbackInfo ci) {
        // Only add our own provider if this is the manager of client
        // resource packs, we wouldn't want to mess with datapacks
        if (sources.stream().anyMatch(FolderRepositorySource.class::isInstance)) {
            var client = Minecraft.getInstance();

            sources = new HashSet<>(sources);
            sources.add(new NestedFolderPackProvider(client.getResourcePackDirectory().toFile()));

            // Load shared resources compat if present
            //if (FabricLoader.getInstance().isModLoaded("shared-resources")) {
            //	sources.add(new ExternalNestedPackSelectionModel.EntryProvider(() -> GameResourceHelper.getPathFor(DefaultGameResources.RESOURCEPACKS)));
            //}
        }
    }
}
