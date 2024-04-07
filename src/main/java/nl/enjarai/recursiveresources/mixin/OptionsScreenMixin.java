package nl.enjarai.recursiveresources.mixin;

import java.nio.file.Path;
import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import nl.enjarai.recursiveresources.gui.FolderedResourcePackScreen;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Shadow
    protected abstract void refreshResourcePacks(PackRepository resourcePackManager);

    /**
     * @author recursiveresources
     * @reason Replace the resource packs screen with a custom one.
     */
    @Overwrite
    private Screen method_47631() {
        var client = Minecraft.getInstance();
        var packRoots = new ArrayList<Path>();
        packRoots.add(client.getResourcePackDirectory());

//        if (FabricLoader.getInstance().isModLoaded("shared-resources")) {
//            var directory = GameResourceHelper.getPathFor(DefaultGameResources.RESOURCEPACKS);
//
//            if (directory != null) {
//                packRoots.add(directory);
//            }
//        }

        return new FolderedResourcePackScreen(
                this, client.getResourcePackRepository(),
                this::refreshResourcePacks, client.getResourcePackDirectory().toFile(),
                Component.translatable("resourcePack.title"),
                packRoots
        );
    }
}
