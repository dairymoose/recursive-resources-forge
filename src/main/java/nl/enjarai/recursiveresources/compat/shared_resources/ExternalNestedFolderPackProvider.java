package nl.enjarai.recursiveresources.compat.shared_resources;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.server.packs.repository.Pack;
import nl.enjarai.recursiveresources.pack.NestedFolderPackProvider;

public class ExternalNestedFolderPackProvider extends NestedFolderPackProvider {
    protected final Supplier<Path> pathSupplier;

    public ExternalNestedFolderPackProvider(Supplier<Path> pathSupplier) {
        super(pathSupplier.get().toFile());
        this.pathSupplier = pathSupplier;
    }

    @Override
    public void loadPacks(Consumer<Pack> profileAdder) {
        Path path = pathSupplier.get();
        if (path == null) return;
        root = path.toFile();
        rootLength = root.getAbsolutePath().length();

        super.loadPacks(profileAdder);
    }
}
