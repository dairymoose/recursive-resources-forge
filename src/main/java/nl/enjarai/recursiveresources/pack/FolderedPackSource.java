package nl.enjarai.recursiveresources.pack;

import java.nio.file.Path;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackSource;

public record FolderedPackSource(Path root, Path file) implements PackSource {
	@Override
	public Component decorate(Component packName) {
		return packName;
	}

	@Override
	public boolean shouldAddAutomatically() {
		return true;
	}
}
