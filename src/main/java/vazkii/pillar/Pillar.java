/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Pillar Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Pillar
 * 
 * Pillar is Open Source and distributed under the
 * [ADD-LICENSE-HERE]
 * 
 * File Created @ [25/06/2016, 18:03:20 (GMT)]
 */
package vazkii.pillar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.Level;

import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import vazkii.pillar.command.CommandPillarReload;
import vazkii.pillar.command.CommandPillarSpawn;

@Mod(modid = Pillar.MOD_ID, name = Pillar.MOD_NAME, version = Pillar.VERSION, dependencies = Pillar.DEPENDENCIES, acceptableRemoteVersions="*")
public class Pillar {

	public static final String MOD_ID = "Pillar";
	public static final String MOD_NAME = "Pillar";
	public static final String BUILD = "GRADLE:BUILD";
	public static final String VERSION = "GRADLE:VERSION-" + BUILD;
	public static final String DEPENDENCIES = "required-after:Forge@[12.17.0.1909,);";

	public static final String TEMPLATE_FILE = "_template.json";

	public static File pillarDir;
	public static File structureDir;
	public static File lootTablesDir;

	public static TemplateManager templateManager;

	public static boolean devMode;
	public static float rarityMultiplier;
	public static int maxStructuresInOneChunk;
	public static int generatorWeight;
	public static int maximumGenerationIterations;
	
	// Obfuscation stuff
	public static final String[] OBF_REGISTERED_LOOT_TABES = {
			"c", "field_186527_c", "registeredLootTables"
	};

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// TODO remove debugs
		System.out.println("Testing 1");
		StructureGenerator.handleFunctions(new Random(), "$run_if(0.5)$ struct pillar:dungeon_centerpiece$rand_i(0;4)$");
		System.out.println("Testing 2");
		StructureGenerator.handleFunctions(new Random(), "spawner $rand_s(Zombie;20;Skeleton;20;Spider;20;Creeper;20)$");
		
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());

		config.load();
		devMode = config.getBoolean("Dev Mode", Configuration.CATEGORY_GENERAL, false, "");
		rarityMultiplier = config.getFloat("Rarity Multiplier", Configuration.CATEGORY_GENERAL, 1F, 0F, Float.MAX_VALUE, "");
		maxStructuresInOneChunk = config.getInt("Max Structures In One Chunk", Configuration.CATEGORY_GENERAL, 1, 1, Integer.MAX_VALUE, "");
		generatorWeight = config.getInt("Generator Weight", Configuration.CATEGORY_GENERAL, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, "The weight to apply to Pillar's generator. Higher weight generators will spawn their structures before other mods'");
		maximumGenerationIterations = config.getInt("Maximum Generation Iterations", Configuration.CATEGORY_GENERAL, 50, 0, Integer.MAX_VALUE, "In a chain of structures spawned by 'struct' data blocks in other structures, how many can be spawned before the chain is put to a halt.");

		if(config.hasChanged())
			config.save();

		pillarDir = new File(event.getModConfigurationDirectory().getParentFile(), "pillar");
		if(!pillarDir.exists())
			pillarDir.mkdir();

		structureDir = new File(pillarDir, "structures");
		if(!structureDir.exists())
			structureDir.mkdir();
		
		lootTablesDir = new File(pillarDir, "loot_tables");
		if(!lootTablesDir.exists())
			lootTablesDir.mkdir();

		File template = new File(pillarDir, TEMPLATE_FILE);
		if(!template.exists()) {
			try {
				template.createNewFile();
				InputStream inStream = Pillar.class.getResourceAsStream("/assets/pillar/" + TEMPLATE_FILE);
				System.out.println(inStream);
				OutputStream outStream = new FileOutputStream(template);
				IOUtils.copy(inStream, outStream);
				inStream.close();
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		StructureLoader.loadStructures(null);
		GameRegistry.registerWorldGenerator(new WorldGenerator(), generatorWeight);
	}
	
	public static void resetManagers() {
		templateManager = new TemplateManager(structureDir.getAbsolutePath());
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandPillarReload());
		event.registerServerCommand(new CommandPillarSpawn());
	}

	public static void log(String m) {
		FMLLog.log(Level.INFO, "[Pillar] %s", m);
	}

}
