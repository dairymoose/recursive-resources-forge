package nl.enjarai.recursiveresources;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(RecursiveResources.MODID)
public class RecursiveResources
{
	public Object client;
	
	public static final String MODID = "recursiveresources";
	
    public static final Logger LOGGER = LogManager.getLogger();
    
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    public RecursiveResources() {
//        DistExecutor.runWhenOn(Dist.CLIENT, () -> new Runnable() {
//		@Override
//		public void run() {
//			client = new XenoTechClient();
//		}});
    }
}
