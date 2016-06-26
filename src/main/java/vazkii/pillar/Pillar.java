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

import org.apache.logging.log4j.Level;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import vazkii.pillar.command.CommandPillarReload;
import vazkii.pillar.command.CommandPillarSpawn;
import vazkii.pillar.proxy.CommonProxy;

@Mod(modid = Pillar.MOD_ID, name = Pillar.MOD_NAME, version = Pillar.VERSION, dependencies = Pillar.DEPENDENCIES)
public class Pillar {

	public static final String MOD_ID = "Pillar";
	public static final String MOD_NAME = "Pillar";
	public static final String BUILD = "GRADLE:BUILD";
	public static final String VERSION = "GRADLE:VERSION-" + BUILD;
	public static final String DEPENDENCIES = "required-after:Forge@[12.17.0.1909,);";

	@SidedProxy(clientSide = "vazkii.pillar.proxy.ClientProxy", serverSide = "vazkii.pillar.proxy.CommonProxy")
	public static CommonProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
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
