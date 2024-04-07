package nl.enjarai.recursiveresources.gui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList.PackEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import nl.enjarai.recursiveresources.RecursiveResources;
import nl.enjarai.recursiveresources.pack.FolderMeta;
import nl.enjarai.recursiveresources.pack.FolderPack;

public class ResourcePackFolderEntry extends PackEntry {
    public static final ResourceLocation WIDGETS_TEXTURE = RecursiveResources.id("textures/gui/widgets.png");
    public static final String UP_TEXT = "..";

    private static final Component BACK_DESCRIPTION = Component.translatable("recursiveresources.folder.back");
    private static final Component FOLDER_DESCRIPTION = Component.translatable("recursiveresources.folder.folder");

    private final FolderedResourcePackScreen ownerScreen;
    public final Path folder;
    public final boolean isUp;
    public final List<TransferableSelectionList.PackEntry> children;
    public final FolderMeta meta;

    private static Function<Path, Path> getIconFileResolver(List<Path> roots, Path folder) {
        return iconPath -> {
            if (iconPath.isAbsolute()) {
                return iconPath;
            } else {
                for (var root : roots) {
                    var iconFile = root
                            .resolve(folder)
                            .resolve(iconPath);

                    if (Files.exists(iconFile)) return iconFile;
                }
            }
            return null;
        };
    }

    public ResourcePackFolderEntry(Minecraft client, TransferableSelectionList list, FolderedResourcePackScreen ownerScreen, Path folder, boolean isUp) {
        super(
                client, list,
                new FolderPack(
                        Component.literal(isUp ? UP_TEXT : String.valueOf(folder.getFileName())),
                        isUp ? BACK_DESCRIPTION : FOLDER_DESCRIPTION,
                        getIconFileResolver(ownerScreen.roots, folder),
                        folder,
                        FolderMeta.loadMetaFile(ownerScreen.roots, folder)
                )
        );
        this.ownerScreen = ownerScreen;
        this.folder = folder;
        this.isUp = isUp;
        this.meta = ((FolderPack) pack).getMeta();
        this.children = isUp ? List.of() : resolveChildren();
    }

    public ResourcePackFolderEntry(Minecraft client, TransferableSelectionList list, FolderedResourcePackScreen ownerScreen, Path folder) {
        this(client, list, ownerScreen, folder, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double relativeMouseX = mouseX - (double) parent.getRowLeft();
        if (getChildren().size() > 0 && relativeMouseX <= 32.0D) {
            enableChildren();
            return true;
        }

        ownerScreen.moveToFolder(folder);
        return true;
    }

    @Override
    public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        if (pack instanceof FolderPack entry) {
        	entry.setHovered(hovered);
        }

        super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

        if (hovered) {
            context.fill(x, y, x + 32, y + 32, 0xa0909090);

            int relativeMouseX = mouseX - x;

            if (getChildren().size() > 0) {
                if (relativeMouseX < 32) {
                	context.blit(WIDGETS_TEXTURE, x, y, 0.0F, 32.0F, 32, 32, 256, 256);
                } else {
                	context.blit(WIDGETS_TEXTURE, x, y, 0.0F, 0.0F, 32, 32, 256, 256);
                }
            }
        }
    }

    public void enableChildren() {
        for (TransferableSelectionList.PackEntry entry : getChildren()) {
            if (entry.pack.canSelect()) {
                entry.pack.select();
            }
        }
    }

    public List<TransferableSelectionList.PackEntry> getChildren() {
        return children;
    }

    private List<TransferableSelectionList.PackEntry> resolveChildren() {
        return parent.children().stream()
                .filter(entry -> !(entry instanceof ResourcePackFolderEntry))
                .filter(entry -> meta.containsEntry(entry, folder))
                .sorted(Comparator.comparingInt(entry -> meta.sortEntry((TransferableSelectionList.PackEntry) entry, folder)).reversed())
                .toList();
    }
}
