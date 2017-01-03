/**
 * This class was created by <TehNut>. It's distributed as
 * part of the Pillar Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Pillar
 *
 * Pillar is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [26/06/2016, 15:14:28 (GMT)]
 */
package vazkii.pillar.command;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import vazkii.pillar.Pillar;

public class CommandPillarCopy extends CommandBase {

	private static final FileFilter NBT_FILTER = (FileFilter) FileFilterUtils.suffixFileFilter(".nbt");

	@Override
	public String getName() {
		return "pillar-copy";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "pillar-copy <sourceFile>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try {
			if (args.length > 0) {
				File structureFolder = server.getActiveAnvilConverter().getFile(server.getFolderName(), "structures");
				if (!structureFolder.exists() || structureFolder.isFile()) {
					throw new CommandException("The world's structure folder could not be found.");
				}

				String requestedName = "";
				if (args.length != 1)
					for (String arg : args)
						requestedName += (requestedName.length() > 0 ? " " : "") + arg;
				else
					requestedName = args[0];

				File[] structures = structureFolder.listFiles(NBT_FILTER);
				for (File file : structures) {
					String fileName = file.getName();
					if (fileName.equalsIgnoreCase(requestedName + ".nbt")) {
						FileUtils.copyFileToDirectory(file, Pillar.structureDir);
						File jsonFile = new File(Pillar.pillarDir, fileName.replaceAll("\\.nbt$", ".json"));
						if (!jsonFile.exists()) {
							try {
								jsonFile.createNewFile();
								InputStream inStream = Pillar.class.getResourceAsStream("/assets/pillar/" + Pillar.TEMPLATE_FILE);
								System.out.println(inStream);
								OutputStream outStream = new FileOutputStream(jsonFile);
								IOUtils.copy(inStream, outStream);
								inStream.close();
								outStream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						sender.sendMessage(new TextComponentString("Successfully copied structure '" + fileName.replaceAll("\\.nbt$", "") + "'").setStyle(new Style().setColor(TextFormatting.GREEN)));
						return;
					}
				}

				throw new CommandException("No structure file by that name was found!");
			} else {
				throw new WrongUsageException("/" + getUsage(sender));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		if (args.length == 1) {
			List<String> files = new ArrayList<>();
			File structureFolder = server.getActiveAnvilConverter().getFile(server.getFolderName(), "structures");
			File[] structures = structureFolder.listFiles(NBT_FILTER);
			for (File structure : structures)
				files.add(structure.getName().replaceAll("\\.nbt$", ""));

			return getListOfStringsMatchingLastWord(args, files);
		}
		return super.getTabCompletions(server, sender, args, pos);
	}

}
