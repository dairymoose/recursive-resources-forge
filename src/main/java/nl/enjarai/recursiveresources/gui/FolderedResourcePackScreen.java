package nl.enjarai.recursiveresources.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import nl.enjarai.recursiveresources.RecursiveResources;
import nl.enjarai.recursiveresources.pack.FolderMeta;
import nl.enjarai.recursiveresources.util.ResourcePackListProcessor;
import nl.enjarai.recursiveresources.util.ResourcePackUtils;

public class FolderedResourcePackScreen extends PackSelectionScreen {
    private static final Path ROOT_FOLDER = Path.of("");

    private static final Component OPEN_PACK_FOLDER = Component.translatable("pack.openFolder");
    private static final Component DONE = Component.translatable("gui.done");
    private static final Component SORT_AZ = Component.translatable("recursiveresources.sort.a-z");
    private static final Component SORT_ZA = Component.translatable("recursiveresources.sort.z-a");
    private static final Component VIEW_FOLDER = Component.translatable("recursiveresources.view.folder");
    private static final Component VIEW_FLAT = Component.translatable("recursiveresources.view.flat");
    private static final Component AVAILABLE_PACKS_TITLE_HOVER = Component.translatable("recursiveresources.availablepacks.title.hover");
    private static final Component SELECTED_PACKS_TITLE_HOVER = Component.translatable("recursiveresources.selectedpacks.title.hover");

    protected final Minecraft client = Minecraft.getInstance();
    protected final Screen parent;

    private final ResourcePackListProcessor listProcessor = new ResourcePackListProcessor(this::reload);
    private Comparator<TransferableSelectionList.PackEntry> currentSorter;

    private TransferableSelectionList originalAvailablePacks;
    private TransferableSelectionList customAvailablePacks;
    private StringWidget searchField;

    private Path currentFolder = ROOT_FOLDER;
    private FolderMeta currentFolderMeta;
    private boolean folderView = true;
    public final List<Path> roots;

    public FolderedResourcePackScreen(Screen parent, PackRepository packManager, Consumer<PackRepository> applier, File mainRoot, Component title, List<Path> roots) {
        super(packManager, applier, mainRoot.toPath(), title);
        this.parent = parent;
        this.roots = roots;
        this.currentFolderMeta = FolderMeta.loadMetaFile(roots, currentFolder);
        this.currentSorter = (pack1, pack2) -> Integer.compare(
                currentFolderMeta.sortEntry(pack1, currentFolder),
                currentFolderMeta.sortEntry(pack2, currentFolder)
        );
    }

    // Components

    @Override
    protected void init() {
        super.init();
        
        findButton(OPEN_PACK_FOLDER).ifPresent(btn -> {
            btn.setX(width / 2 + 25);
            btn.setY(height - 48);
        });

        findButton(DONE).ifPresent(btn -> {
            btn.setX(width / 2 + 25);
            btn.setY(height - 26);
            if (btn instanceof Button button) {
            	button.onPress = btn2 -> onClose();
            }
        });

        addRenderableWidget(
                Button.builder(folderView ? VIEW_FOLDER : VIEW_FLAT, btn -> {
                    folderView = !folderView;
                    btn.setMessage(folderView ? VIEW_FOLDER : VIEW_FLAT);

                    reload();
                    customAvailablePacks.setScrollAmount(0.0);
                })
                .bounds(width / 2 - 179, height - 26, 154, 20)
                .build()
        );

        searchField = addRenderableWidget(new StringWidget(
                width / 2 - 179, height - 46, 154, 16, Component.literal(""), this.font));
        searchField.setFocused(true);
        //searchField.setChangedListener(listProcessor::setFilter);
        addRenderableWidget(searchField);

        // Replacing the available pack list with our custom implementation
        originalAvailablePacks = availablePackList;
        removeWidget(originalAvailablePacks);
        updateFocus(customAvailablePacks = new TransferableSelectionList(client, this, 200, height, availablePackList.title));
        customAvailablePacks.setLeftPos(width / 2 - 204);
        // Make the title of the available packs selector clickable to load all packs
        ((FolderedTransferableSelectionList) customAvailablePacks).recursiveresources$setTitleClickable(AVAILABLE_PACKS_TITLE_HOVER, null, () -> {
            for (TransferableSelectionList.PackEntry entry : Lists.reverse(List.copyOf(availablePackList.children()))) {
                if (entry.pack.canSelect()) {
                    entry.pack.select();
                }
            }
        });
        availablePackList = customAvailablePacks;

        // Also make the selected packs title clickable to unload them
        ((FolderedTransferableSelectionList) selectedPackList).recursiveresources$setTitleClickable(SELECTED_PACKS_TITLE_HOVER, null, () -> {
            for (TransferableSelectionList.PackEntry entry : List.copyOf(selectedPackList.children())) {
                if (entry.pack.canUnselect()) {
                    entry.pack.unselect();
                }
            }
        });

        listProcessor.pauseCallback();
        listProcessor.setSorter(currentSorter == null ? (currentSorter = ResourcePackListProcessor.sortAZ) : currentSorter);
        listProcessor.setFilter(searchField.getMessage().getString()); //wrong?
        listProcessor.resumeCallback();
    }

    private Optional<AbstractButton> findButton(Component text) {
        return children.stream()
                .filter(AbstractButton.class::isInstance)
                .map(AbstractButton.class::cast)
                .filter(btn -> text.equals(btn.getMessage()))
                .findFirst();
    }

    @Override
    public void populateLists() {
        super.populateLists();
        if (customAvailablePacks != null) {
            onFiltersUpdated();
        }
    }

    // Processing

    private Path getParentFileSafe(Path file) {
        var parent = file.getParent();
        return parent == null ? ROOT_FOLDER : parent;
    }

    private boolean notInRoot() {
        return folderView && !currentFolder.equals(ROOT_FOLDER);
    }

    private void onFiltersUpdated() {
        List<TransferableSelectionList.PackEntry> folders = null;

        if (folderView) {
            folders = new ArrayList<>();

            // add a ".." entry when not in the root folder
            if (notInRoot()) {
                folders.add(new ResourcePackFolderEntry(client, customAvailablePacks,
                        this, getParentFileSafe(currentFolder), true));
            }

            // create entries for all the folders that aren't packs
            var createdFolders = new ArrayList<Path>();
            for (Path root : roots) {
                var absolute = root.resolve(currentFolder);

                try (var contents = Files.list(absolute)) {
                    for (Path folder : contents.filter(ResourcePackUtils::isFolderButNotFolderBasedPack).toList()) {
                        var relative = root.relativize(folder.normalize());

                        if (createdFolders.contains(relative)) {
                            continue;
                        }

                        var entry = new ResourcePackFolderEntry(client, customAvailablePacks,
                                this, relative);

                        if (((PackSelectionModel.Entry) entry.pack).notHidden()) {
                            folders.add(entry);
                        }
                        createdFolders.add(relative);
                    }
                } catch (IOException e) {
                    RecursiveResources.LOGGER.error("Failed to read contents of " + absolute, e);
                }
            }
        }

        listProcessor.apply(customAvailablePacks.children().stream().toList(), folders, customAvailablePacks.children());

        // filter out all entries that aren't in the current folder
        if (folderView) {
            var filteredPacks = customAvailablePacks.children().stream().filter(entry -> {
                // if it's a folder, it's already relative, so we can check easily
                if (entry instanceof ResourcePackFolderEntry folder) {
                    return folder.isUp || currentFolder.equals(getParentFileSafe(folder.folder));
                }

                // if it's a pack, we can use the foldermeta to check if it should be shown
                return currentFolderMeta.containsEntry(entry, currentFolder);
            }).toList();

            customAvailablePacks.children().clear();
            customAvailablePacks.children().addAll(filteredPacks);
        }

        customAvailablePacks.setScrollAmount(customAvailablePacks.getScrollAmount());
    }

    public void moveToFolder(Path folder) {
        currentFolder = folder;
        currentFolderMeta = FolderMeta.loadMetaFile(roots, currentFolder);
        reload();
        customAvailablePacks.setScrollAmount(0.0);
    }

    // UI Overrides

    @Override
    public void tick() {
        super.tick();
        //searchField.
    }

    protected void applyAndClose() {
    	this.model.commit();
        closeWatcher();
    }

    @Override
    public void onClose() {
        closeWatcher();
        client.setScreen(parent);
        client.options.loadSelectedResourcePacks(client.getResourcePackRepository());
    }
}
