/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Pillar Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Pillar
 * 
 * Pillar is Open Source and distributed under the
 * [ADD-LICENSE-HERE]
 * 
 * File Created @ [25/06/2016, 21:09:43 (GMT)]
 */
package vazkii.pillar.command;

import com.sun.jna.Structure;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import vazkii.pillar.StructureLoader;

public class CommandPillarReload extends CommandBase {

	@Override
	public String getCommandName() {
		return "pillar-reload";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		StructureLoader.loadStructures(sender.getEntityWorld());
		
		sender.addChatMessage(new TextComponentString("Reloaded structures. There are " + StructureLoader.loadedSchemas.size() + " structures currently loaded."));
	}
	
	// TODO lower level

}
