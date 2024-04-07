package nl.enjarai.recursiveresources.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;
import nl.enjarai.recursiveresources.RecursiveResources;

public class ResourcePackUtils {
    private static final File[] EMPTY_FILE_ARRAY = new File[0];
    // Catches all characters that are not allowed in a path on Windows and Linux
    private static final String UNSAFE_PATH_REGEX = "[<>:\"/\\\\|?*]";

    public static File[] wrap(File[] filesOrNull) {
        return filesOrNull == null ? EMPTY_FILE_ARRAY : filesOrNull;
    }

    public static boolean isFolderBasedPack(File folder) {
        return new File(folder, "pack.mcmeta").exists();
    }

    public static boolean isFolderBasedPack(Path folder) {
        return Files.exists(folder.resolve("pack.mcmeta"));
    }

    public static boolean isFolderButNotFolderBasedPack(File folder) {
        return folder.isDirectory() && !isFolderBasedPack(folder);
    }

    public static boolean isFolderButNotFolderBasedPack(Path folder) {
        return Files.isDirectory(folder) && !isFolderBasedPack(folder);
    }

    public static boolean isPack(Path fileOrFolder) {
        return Files.isDirectory(fileOrFolder) ? isFolderBasedPack(fileOrFolder) : fileOrFolder.toString().endsWith(".zip");
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Path determinePackFolder(PackResources pack) {
        try {
            if (pack instanceof PathPackResources directoryResourcePack) {
                return directoryResourcePack.root;
            } else if (pack instanceof FilePackResources zipResourcePack) {
                return zipResourcePack.file.toPath();
            //} else if (pack instanceof ModNioResourcePack modResourcePack) {
            //    return Path.of(modResourcePack.getName().replaceAll(UNSAFE_PATH_REGEX, "_"));
            } else {
                RecursiveResources.LOGGER.warn("Failed to determine source folder for pack: " + pack.packId() + ", unknown pack type: " + pack.getClass().getName());
                return null;
            }
        } catch (Exception e) {
            RecursiveResources.LOGGER.error("Error determining source folder for pack: " + pack.packId(), e);
            return null;
        }
    }

    public static boolean isChildOfFolder(Path folder, PackResources pack) {
        var packFolder = determinePackFolder(pack);
        return packFolder != null && packFolder.toAbsolutePath().startsWith(folder.toAbsolutePath());
    }
}
