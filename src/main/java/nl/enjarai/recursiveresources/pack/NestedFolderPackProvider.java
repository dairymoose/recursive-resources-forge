package nl.enjarai.recursiveresources.pack;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.common.data.ExistingFileHelper.ResourceType;
import nl.enjarai.recursiveresources.util.ResourcePackUtils;

public class NestedFolderPackProvider implements RepositorySource {
    protected File root;
    protected int rootLength;

    public NestedFolderPackProvider(File root) {
        this.root = root;
        this.rootLength = root.getAbsolutePath().length();
    }

    @Override
    public void loadPacks(Consumer<Pack> profileAdder) {
        File[] folders = root.listFiles(ResourcePackUtils::isFolderButNotFolderBasedPack);

        for (File folder : ResourcePackUtils.wrap(folders)) {
            processFolder(folder, profileAdder);
        }
    }

    public void processFolder(File folder, Consumer<Pack> profileAdder) {
        if (ResourcePackUtils.isFolderBasedPack(folder)) {
            addPack(folder, profileAdder);
            return;
        }

        File[] zipFiles = folder.listFiles(file -> file.isFile() && file.getName().endsWith(".zip"));

        for (File zipFile : ResourcePackUtils.wrap(zipFiles)) {
            addPack(zipFile, profileAdder);
        }

        File[] nestedFolders = folder.listFiles(File::isDirectory);

        for (File nestedFolder : ResourcePackUtils.wrap(nestedFolders)) {
            processFolder(nestedFolder, profileAdder);
        }
    }

    public void addPack(File fileOrFolder, Consumer<Pack> profileAdder) {
        String displayName = fileOrFolder.getName();
        String name = "file/" + StringUtils.removeStart(
                fileOrFolder.getAbsolutePath().substring(rootLength).replace('\\', '/'), "/");
        Pack info;
        Path rootPath = root.toPath();
        Path filePath = rootPath.relativize(fileOrFolder.toPath());
        FolderedPackSource packSource = new FolderedPackSource(rootPath, filePath);

        if (fileOrFolder.isDirectory()) {
            info = Pack.readMetaAndCreate(
                    name, Component.literal(displayName), false,
                    (packName) -> new PathPackResources(packName, fileOrFolder.toPath(), true),
                    PackType.CLIENT_RESOURCES, Position.TOP, packSource
            );
        } else {
            info = Pack.readMetaAndCreate(
                    name, Component.literal(displayName), false,
                    (packName) -> new FilePackResources(packName, fileOrFolder, true),
                    PackType.CLIENT_RESOURCES, Position.TOP, packSource
            );
        }

        if (info != null) {
            profileAdder.accept(info);
        }
    }
}
