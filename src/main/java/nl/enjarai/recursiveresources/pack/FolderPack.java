package nl.enjarai.recursiveresources.pack;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import nl.enjarai.recursiveresources.RecursiveResources;

public class FolderPack implements PackSelectionModel.Entry {
    private static final ResourceLocation FOLDER_TEXTURE = RecursiveResources.id("textures/gui/folder.png");
    private static final ResourceLocation OPEN_FOLDER_TEXTURE = RecursiveResources.id("textures/gui/folder_open.png");

    private static ResourceLocation loadCustomIcon(Path icon, Path relativeFolder) {
        if (icon != null && Files.exists(icon)) {
            try (InputStream stream = Files.newInputStream(icon)) {
                // Get the path relative to the resourcepacks directory
                var relativePath = relativeFolder.toString();

                // Ensure the path only contains "a-z0-9_.-" characters
                relativePath = relativePath.toLowerCase().replaceAll("[^a-zA-Z0-9_.-]", "_");

                ResourceLocation id = new ResourceLocation("recursiveresources", "textures/gui/custom_folders/" + relativePath + "icon.png");
                Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(NativeImage.read(stream)));
                return id;
            } catch (Exception e) {
                RecursiveResources.LOGGER.warn("Error loading custom folder icon:");
                e.printStackTrace();
            }
        }
        return null;
    }

    private final Component displayName;
    private final Component description;
    @Nullable
    private ResourceLocation icon = null;
    private final FolderMeta meta;

    private boolean hovered = false;

    public FolderPack(Component displayName, Component description, Function<Path, Path> iconFileResolver, Path relativeFolder, FolderMeta meta) {
        this.displayName = displayName;
        if (meta.description().equals("")) {
            this.description = description;
        } else {
            this.description = Component.literal(meta.description());
        }
        this.icon = loadCustomIcon(iconFileResolver.apply(meta.icon()), relativeFolder);
        this.meta = meta;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    @Override
    public ResourceLocation getIconTexture() {
        return icon != null ? icon : (hovered ? OPEN_FOLDER_TEXTURE : FOLDER_TEXTURE);
    }

    @Override
    public PackCompatibility getCompatibility() {
        return PackCompatibility.COMPATIBLE;
    }

    @Override
    public String getId() {
        return displayName.getString();
    }

    @Override
    public Component getTitle() {
        return displayName;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public PackSource getPackSource() {
        return PackSource.DEFAULT;
    }

    @Override
    public boolean isFixedPosition() {
        return true;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

//    @Override
//    public void updateHighContrastOptionInstance() {
//
//    }

    @Override
    public void moveUp() {

    }

    @Override
    public void moveDown() {

    }

    @Override
    public boolean notHidden() {
        return false;
    }

    @Override
    public boolean canMoveUp() {
        return false;
    }

    @Override
    public boolean canMoveDown() {
        return false;
    }

    public FolderMeta getMeta() {
        return meta;
    }

    public boolean isVisible() {
        return !meta.hidden();
    }

	@Override
	public boolean isSelected() {
		return false;
	}

	@Override
	public void select() {
		// TODO Auto-generated method stub
	}

	@Override
	public void unselect() {
		// TODO Auto-generated method stub
	}
}
