/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Pillar Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Pillar
 * 
 * Pillar is Open Source and distributed under the
 * [ADD-LICENSE-HERE]
 * 
 * File Created @ [25/06/2016, 18:05:01 (GMT)]
 */
package vazkii.pillar.proxy;

import java.io.File;

import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import scala.reflect.api.StandardLiftables.StandardUnliftableInstances;
import vazkii.pillar.StructureLoader;

public class CommonProxy {

	public static File pillarDir;
	public static File structureDir;
	public static TemplateManager templateManager;
	
	public static boolean devMode;
	
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		
		config.load();
		devMode = config.getBoolean("Dev Mode", Configuration.CATEGORY_GENERAL, false, "");
		
		if(config.hasChanged())
			config.save();
		
		pillarDir = new File(event.getModConfigurationDirectory().getParentFile(), "pillar");
		if(!pillarDir.exists())
			pillarDir.mkdir();
		
		structureDir = new File(pillarDir, "structures");
		if(!structureDir.exists())
			structureDir.mkdir();
		
		templateManager = new TemplateManager(structureDir.getAbsolutePath());
		
		StructureLoader.loadStructures();
	}
	
}
