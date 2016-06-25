/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Pillar Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Pillar
 * 
 * Pillar is Open Source and distributed under the
 * [ADD-LICENSE-HERE]
 * 
 * File Created @ [25/06/2016, 18:42:33 (GMT)]
 */
package vazkii.pillar;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import vazkii.pillar.proxy.CommonProxy;
import vazkii.pillar.schema.StructureSchema;

public final class StructureGenerator {

	public static boolean placeStructureAtPosition(Random rand, StructureSchema schema, WorldServer world, BlockPos pos) {
		if(pos == null)
			return false;

		MinecraftServer minecraftserver = world.getMinecraftServer();
		TemplateManager templatemanager = CommonProxy.templateManager;
		Template template = templatemanager.func_189942_b(minecraftserver, new ResourceLocation(schema.structureName));

		if(template == null)
			return false;

		if(CommonProxy.devMode)
			Pillar.log("Generating Structure " +  schema.structureName + " at " + pos);

		PlacementSettings settings = new PlacementSettings();
		settings.setMirror(schema.mirrorType);

		Rotation rot = schema.rotation;
		if(schema.rotation == null)
			rot = Rotation.values()[rand.nextInt(Rotation.values().length)];

		settings.setRotation(rot);
		settings.setIgnoreEntities(schema.ignoreEntities);
		settings.setChunk((ChunkPos) null);
		settings.setReplacedBlock((Block) null);
		settings.setIgnoreStructureBlock(false);

		settings.func_189946_a(MathHelper.clamp_float(schema.integrity, 0.0F, 1.0F));

		BlockPos finalPos = pos.add(schema.offsetX, schema.offsetY, schema.offsetZ);
		template.addBlocksToWorldChunk(world, finalPos, settings);

		BlockPos size = template.getSize();
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
						if(currState.getBlock().isAir(currState, world, currPos))
							continue;
						
						int k = -1;
						while(true) {
							BlockPos checkPos = currPos.add(0, k, 0);
							IBlockState state = world.getBlockState(checkPos);
							if(state.getBlock().isAir(state, world, checkPos) || state.getBlock().isReplaceable(world, checkPos)) {
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

		return true;
	}


}
