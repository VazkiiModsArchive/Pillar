/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Pillar Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Pillar
 * 
 * Pillar is Open Source and distributed under the
 * [ADD-LICENSE-HERE]
 * 
 * File Created @ [25/06/2016, 21:09:39 (GMT)]
 */
package vazkii.pillar.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import vazkii.pillar.StructureGenerator;
import vazkii.pillar.StructureLoader;
import vazkii.pillar.schema.StructureSchema;

public class CommandPillarSpawn extends CommandBase {

	@Override
	public String getCommandName() {
		return "pillar-spawn";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "pillar-spawn <structure name> <x> <y> <z>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length != 4)
			throw new CommandException("Wrong argument length.");
		
		String name = args[0];
        BlockPos pos = parseBlockPos(sender, args, 1, false);

		StructureSchema schema = StructureLoader.loadedSchemas.get(name);
		if(schema == null)
			throw new CommandException("There's no structure with that name.");
		
		World world = sender.getEntityWorld();
		if(world instanceof WorldServer)
			StructureGenerator.placeStructureAtPosition(world.rand, schema, (WorldServer) world, pos);
		
		sender.addChatMessage(new TextComponentString("Placed down structure " + name));
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

}
