/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Pillar Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Pillar
 * 
 * Pillar is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * 
 * File Created @ [25/06/2016, 18:42:33 (GMT)]
 */
package vazkii.pillar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import vazkii.pillar.schema.FillingType;
import vazkii.pillar.schema.StructureSchema;

public final class StructureGenerator {

	private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\$(.*?)\\((.*?)\\)\\$");
	private static final Pattern TOKENIZING_PATTERN = Pattern.compile("\\s*(?<!\\\\);\\s*");

	private static final HashMap<String, DataHandler> dataHandlers = new HashMap();
	private static final HashMap<String, Function> functions = new HashMap();

	private static int iteration;

	static {
		dataHandlers.put("run", StructureGenerator::commandRun);
		dataHandlers.put("chest", StructureGenerator::commandChest);
		dataHandlers.put("spawner", StructureGenerator::commandSpawner);
		dataHandlers.put("struct", StructureGenerator::commandStruct);
		dataHandlers.put("load_loot_table", StructureGenerator::commandLoadLootTable);

		functions.put("rand_i", StructureGenerator::functionRandomInteger);
		functions.put("rand_s", StructureGenerator::functionRandomString);
		functions.put("run_if", StructureGenerator::functionRunIf);
	}

	public static boolean placeStructureAtPosition(Random rand, StructureSchema schema, Rotation baseRotation, WorldServer world, BlockPos pos, boolean useSchemaRotation) {
		return placeStructureAtPosition(rand, schema, baseRotation, world, pos, 0, useSchemaRotation);
	}

	public static boolean placeStructureAtPosition(Random rand, StructureSchema schema, Rotation baseRotation, WorldServer world, BlockPos pos, int iteration, boolean useSchemaRotation) {
		if(pos == null)
			return false;

		if(iteration > Pillar.maximumGenerationIterations)
			return false;

		MinecraftServer minecraftserver = world.getMinecraftServer();
		TemplateManager templatemanager = Pillar.templateManager;
		Template template = templatemanager.func_189942_b(minecraftserver, new ResourceLocation(schema.structureName));

		if(template == null)
			return false;

		BlockPos size = template.getSize();
		int top = pos.getY() + size.getY(); 
		if(top >= 256) {
			int shift = top - 256;
			pos.add(0, -shift, 0);
		}

		if(Pillar.devMode && iteration == 0)
			Pillar.log("Generating Structure " +  schema.structureName + " at " + pos);

		PlacementSettings settings = new PlacementSettings();
		settings.setMirror(schema.getMirrorType());

		Rotation rot;

		if(useSchemaRotation && baseRotation != null) {
			rot = schema.getRotation();
			if(schema.rotation == null)
				rot = Rotation.values()[rand.nextInt(Rotation.values().length)];
			rot = rot.add(baseRotation);
		} else {
			rot = baseRotation;
			if(rot == null)
				rot = Rotation.NONE;
		}

		settings.setRotation(rot);
		settings.setIgnoreEntities(schema.ignoreEntities);
		settings.setChunk((ChunkPos) null);
		settings.setReplacedBlock((Block) null);
		settings.setIgnoreStructureBlock(false);

		settings.func_189946_a(MathHelper.clamp_float(schema.integrity, 0.0F, 1.0F));

		BlockPos finalPos = pos.add(schema.offsetX, schema.offsetY, schema.offsetZ);
		template.addBlocksToWorldChunk(world, finalPos, settings);

		if(schema.decay > 0) {
			for(int i = 0; i < size.getX(); i++)
				for(int j = 0; j < size.getY(); j++)
					for(int k = 0; k < size.getZ(); k++) {
						BlockPos currPos = finalPos.add(template.transformedBlockPos(settings, new BlockPos(i, j, k)));
						IBlockState state = world.getBlockState(currPos);
						if(state.getBlock() == Blocks.STONEBRICK && state.getValue(BlockStoneBrick.VARIANT) == BlockStoneBrick.EnumType.DEFAULT && rand.nextFloat() < schema.decay)
							world.setBlockState(currPos, state.withProperty(BlockStoneBrick.VARIANT, rand.nextBoolean() ? BlockStoneBrick.EnumType.MOSSY : BlockStoneBrick.EnumType.CRACKED));
					}
		}

		if(schema.filling != null && !schema.filling.isEmpty()) {
			Block block = Block.getBlockFromName(schema.filling);
			if(block != null)
				for(int i = 0; i < size.getX(); i++)
					for(int j = 0; j < size.getZ(); j++) {
						BlockPos currPos = finalPos.add(template.transformedBlockPos(settings, new BlockPos(i, 0, j)));
						IBlockState currState = world.getBlockState(currPos);
						if(currState.getBlock().isAir(currState, world, currPos) || currState.getBlock() == Blocks.STRUCTURE_BLOCK)
							continue;

						FillingType type = schema.fillingType;
						if(type == null)
							type = FillingType.AIR;

						int k = -1;
						while(true) {
							BlockPos checkPos = currPos.add(0, k, 0);
							IBlockState state = world.getBlockState(checkPos);
							if(type.canFill(world, state, checkPos)) {
								IBlockState newState = block.getStateFromMeta(schema.fillingMetadata);

								if(schema.decay > 0 && newState.getBlock() == Blocks.STONEBRICK && newState.getValue(BlockStoneBrick.VARIANT) == BlockStoneBrick.EnumType.DEFAULT && rand.nextFloat() < schema.decay)
									newState = newState.withProperty(BlockStoneBrick.VARIANT, rand.nextBoolean() ? BlockStoneBrick.EnumType.MOSSY : BlockStoneBrick.EnumType.CRACKED);

								world.setBlockState(checkPos, newState);
							} else break;

							if(checkPos.getY() == 0)
								break;

							k--;
						}
					}
		}

		Map<BlockPos, String> dataBlocks = template.getDataBlocks(finalPos, settings);

		for(Entry<BlockPos, String> entry : dataBlocks.entrySet()) {
			BlockPos entryPos = entry.getKey();
			String data = entry.getValue();
			world.setBlockState(entryPos, Blocks.AIR.getDefaultState());
			handleData(rand, schema, settings, entryPos, data, world, iteration);
		}

		return true;
	}

	private static void handleData(Random rand, StructureSchema schema, PlacementSettings settings, BlockPos pos, String data, WorldServer world, int iteration) {
		if(data == null || data.isEmpty())
			return;

		data = handleFunctions(rand, data);

		data = data.replaceAll("\\/\\*\\*.*", "").trim();
		String command = data.replaceAll("\\s.*", "").toLowerCase();

		if(dataHandlers.containsKey(command)) {
			data = data.replaceAll("^.*?\\s", "");
			dataHandlers.get(command).handleData(rand, schema, settings, pos, data, world, iteration);
		}
	}

	public static String handleFunctions(Random rand, String data) {
		while(true) {
			Pair<Integer, Integer> boundaries = findFunction(data);
			if(boundaries == null)
				break;

			String functionStr = data.substring(boundaries.getLeft(), boundaries.getRight());

			functionStr = functionStr.substring(1, functionStr.length() - 2);

			int opener = functionStr.indexOf("(");
			String functionName = functionStr.substring(0, opener);
			String params = functionStr.substring(opener + 1);

			String result = "";
			Function function = functions.get(functionName.toLowerCase());

			if(function != null)
				try {
					result = function.handle(tokenize(params), rand);
				} catch(IllegalArgumentException e) {
					e.printStackTrace();
				}

			data = data.substring(0, boundaries.getLeft()) + result + data.substring(boundaries.getRight());
		}

		return data;
	}

	private static void commandRun(Random rand, StructureSchema schema, PlacementSettings settings, BlockPos pos, String data, WorldServer world, int iteration) {
		StructureCommandSender.world = world;
		StructureCommandSender.position = pos;

		if(data.startsWith("/"))
			data = data.substring(1);

		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		server.getCommandManager().executeCommand(StructureCommandSender.INSTANCE, data);
	}

	private static void commandChest(Random rand, StructureSchema schema, PlacementSettings settings, BlockPos pos, String data, WorldServer world, int iteration) {
		String[] tokens = data.split("\\s");

		if(tokens.length == 0)
			return;

		String orientation = tokens.length == 1 ? "" : tokens[0];
		String lootTable = tokens.length == 1 ? tokens[0] : tokens[1];

		EnumFacing facing = EnumFacing.byName(orientation);
		if(facing == null)
			facing = EnumFacing.NORTH;

		facing = settings.getRotation().rotate(facing);

		world.setBlockState(pos, Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, facing));
		TileEntityChest chest = (TileEntityChest) world.getTileEntity(pos);

		ResourceLocation res = new ResourceLocation(lootTable);
		if(res.getResourceDomain().equals("pillar"))
			StructureLoader.copyNeededLootTable(world, res.getResourcePath());

		chest.setLootTable(res, rand.nextLong());
	}

	private static void commandLoadLootTable(Random rand, StructureSchema schema, PlacementSettings settings, BlockPos pos, String data, WorldServer world, int iteration) {
		String[] tokens = data.split("\\s");

		if(tokens.length == 0)
			return;

		String lootTable = tokens[0];
		StructureLoader.copyNeededLootTable(world, lootTable);
	}

	private static void commandSpawner(Random rand, StructureSchema schema, PlacementSettings settings, BlockPos pos, String data, WorldServer world, int iteration) {
		String[] tokens = data.split("\\s");

		if(tokens.length == 0)
			return;

		world.setBlockState(pos, Blocks.MOB_SPAWNER.getDefaultState());

		TileEntityMobSpawner spawner = (TileEntityMobSpawner) world.getTileEntity(pos);
		spawner.getSpawnerBaseLogic().setEntityName(tokens[0]);
	}

	private static void commandStruct(Random rand, StructureSchema schema, PlacementSettings settings, BlockPos pos, String data, WorldServer world, int iteration) {
		String[] tokens = data.split("\\s");

		if(tokens.length == 0)
			return;

		String structureName = tokens[0];

		StructureSchema newSchema = StructureLoader.loadedSchemas.get(structureName);
		if(newSchema == null || newSchema == schema)
			return;

		int offX = 0, offY = 0, offZ = 0;

		if(tokens.length >= 4) {
			offX = toInt(tokens[1], 0);
			offY = toInt(tokens[2], 0);
			offZ = toInt(tokens[3], 0);
		}

		Rotation rotation = Rotation.NONE;

		if(tokens.length >= 5) {
			String s = tokens[4];
			switch(s) {
			case "90": 
			case "-270":
				rotation = Rotation.CLOCKWISE_90;
				break;
			case "180":
			case "-180":
				rotation = Rotation.CLOCKWISE_180;
				break;
			case "270":
			case "-90":
				rotation = Rotation.COUNTERCLOCKWISE_90;
				break;
			}
		}
		rotation = rotation.add(settings.getRotation());

		BlockPos finalPos = pos.add(offX, offY, offZ);
		placeStructureAtPosition(rand, newSchema, rotation, world, finalPos, iteration + 1, false);
	}

	private static String functionRandomInteger(String[] params, Random rand) {
		if(params.length != 2)
			throw new IllegalArgumentException("rand_i function needs two number parameters");

		int lower = Integer.parseInt(params[0]);
		int upper = Integer.parseInt(params[1]);

		if(upper < lower) {
			int i = lower;
			lower = upper;
			upper = i;
		}

		int diff = upper - lower;
		if(diff == 0)
			return Integer.toString(lower);

		return Integer.toString(rand.nextInt(diff) + lower);
	}

	private static String functionRandomString(String[] params, Random rand) {
		if(params.length % 2 != 0)
			throw new IllegalArgumentException("rand_s function needs an even number of parameters");

		List<WeightedString> strings = new ArrayList();
		int len = params.length / 2;

		for(int i = 0; i < len; i++) {
			String s = params[i * 2];
			int w = Integer.parseInt(params[i * 2 + 1]);
			strings.add(new WeightedString(w, s));
		}

		return WeightedRandom.getRandomItem(rand, strings).s;
	}

	private static String functionRunIf(String[] params, Random rand) {
		if(params.length != 1)
			throw new IllegalArgumentException("run_if function needs a single parameter");

		double chance = Double.parseDouble(params[0]);
		if(rand.nextDouble() < chance)
			return "/**";

		return "";
	}

	private static String[] tokenize(String data) {
		Matcher matcher = TOKENIZING_PATTERN.matcher(data);
		if(!matcher.find())
			return new String[] { data };

		return data.split(TOKENIZING_PATTERN.pattern());
	}

	private static Pair<Integer, Integer> findFunction(String s) {
		Matcher matcher = FUNCTION_PATTERN.matcher(s);
		if(!matcher.find())
			return null;

		return Pair.of(matcher.start(), matcher.end());
	}

	private static int toInt(String s, int def) {
		try {
			int i = Integer.parseInt(s);
			return i;
		} catch(NumberFormatException e) {
			return def;
		}
	}

	private static interface DataHandler {
		public void handleData(Random rand, StructureSchema schema, PlacementSettings settings, BlockPos pos, String data, WorldServer world, int iteration);
	}

	private static interface Function {
		public String handle(String[] params, Random rand);
	}

	private static class WeightedString extends WeightedRandom.Item {

		public final String s;

		public WeightedString(int itemWeightIn, String s) {
			super(itemWeightIn);
			this.s = s;
		}

	}

	public static class StructureCommandSender implements ICommandSender {

		public static final StructureCommandSender INSTANCE = new StructureCommandSender();

		public static World world;
		public static BlockPos position;

		@Override
		public void addChatMessage(ITextComponent p_145747_1_) {
			// NO-OP
		}

		@Override
		public boolean canCommandSenderUseCommand(int p_70003_1_, String p_70003_2_) {
			return p_70003_1_ <= 2;
		}

		@Override
		public World getEntityWorld() {
			return world;
		}

		@Override
		public String getName() {
			return "Pillar-executor";
		}

		@Override
		public ITextComponent getDisplayName() {
			return null;
		}

		@Override
		public BlockPos getPosition() {
			return position;
		}

		@Override
		public Entity getCommandSenderEntity() {
			return null;
		}

		@Override
		public boolean sendCommandFeedback() {
			return false;
		}

		@Override
		public void setCommandStat(Type type, int amount) {
			// NO-OP
		}

		@Override
		public Vec3d getPositionVector() {
			return new Vec3d(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5);
		}

		@Override
		public MinecraftServer getServer() {
			return world.getMinecraftServer();
		}

	}

}

