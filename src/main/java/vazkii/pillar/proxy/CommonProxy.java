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

import com.typesafe.config.Config;

import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import scala.reflect.api.StandardLiftables.StandardUnliftableInstances;
import vazkii.pillar.StructureLoader;
import vazkii.pillar.WorldGenerator;

public class CommonProxy {

	public static File pillarDir;
	public static File structureDir;
	public static TemplateManager templateManager;
	
	public static boolean devMode;
	public static float rarityMultiplier;
	public static int maxStructuresInOneChunk;
	public static int generatorWeight;

	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		
		config.load();
		devMode = config.getBoolean("Dev Mode", Configuration.CATEGORY_GENERAL, false, "");
		rarityMultiplier = config.getFloat("Rarity Multiplier", Configuration.CATEGORY_GENERAL, 1F, 0F, Float.MAX_VALUE, "");
		maxStructuresInOneChunk = config.getInt("Max Structures In One Chunk", Configuration.CATEGORY_GENERAL, 1, 1, Integer.MAX_VALUE, "");
		generatorWeight = config.getInt("Generator Weight", Configuration.CATEGORY_GENERAL, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, "The weight to apply to Pillar's generator. Higher weight generators will spawn their structures before other mods'");
		
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
		GameRegistry.registerWorldGenerator(new WorldGenerator(), generatorWeight);
	}
	
}
