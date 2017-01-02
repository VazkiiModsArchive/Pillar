/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Pillar Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Pillar
 * 
 * Pillar is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * 
 * File Created @ [25/06/2016, 21:09:43 (GMT)]
 */
package vazkii.pillar.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import vazkii.pillar.StructureLoader;

public class CommandPillarReload extends CommandBase {

	@Override
	public String getName() {
		return "pillar-reload";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		StructureLoader.loadStructures(sender.getEntityWorld());
		
		sender.sendMessage(new TextComponentString("Reloaded structures. There are " + StructureLoader.loadedSchemas.size() + " structures currently loaded.").setStyle(new Style().setColor(TextFormatting.GREEN)));
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
}
