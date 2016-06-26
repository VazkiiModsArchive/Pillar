/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Pillar Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Pillar
 * 
 * Pillar is Open Source and distributed under the
 * [ADD-LICENSE-HERE]
 * 
 * File Created @ [25/06/2016, 19:13:22 (GMT)]
 */
package vazkii.pillar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;
import vazkii.pillar.schema.GeneratorType;
import vazkii.pillar.schema.StructureSchema;

public class WorldGenerator implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if(!(world instanceof WorldServer))
			return;

		int structuresGenerated = 0;
		List<StructureSchema> schemaList = new ArrayList(StructureLoader.loadedSchemas.values());
		Collections.shuffle(schemaList, random);
		for(StructureSchema schema : schemaList) {
			EnumActionResult res = generateStructure(schema, random, world, chunkX, chunkZ);
			if(res == EnumActionResult.PASS)
				continue;

			if(res == EnumActionResult.SUCCESS)
				structuresGenerated++;

			if(structuresGenerated >= Pillar.maxStructuresInOneChunk)
				break;
		}
	}

	public EnumActionResult generateStructure(StructureSchema schema, Random random, World world, int chunkX, int chunkZ) {
		if(schema.generatorType == GeneratorType.NONE)
			return EnumActionResult.PASS;

		int rarity = (int) (schema.rarity * Pillar.rarityMultiplier);
		if(rarity > 0 && random.nextInt(rarity) == 0) {
			int x = chunkX * 16 + random.nextInt(16);
			int z = chunkZ * 16 + random.nextInt(16);
			BlockPos xzPos = new BlockPos(x, 0, z);
			BlockPos pos = schema.generatorType.getGenerationPosition(schema, random, world, xzPos);

			if(pos != null) {
				IBlockState state = world.getBlockState(pos);

				if(schema.generatorType.shouldFindLowestBlock())
					while(state.getBlock().isReplaceable(world, pos) && !(state.getBlock() instanceof BlockLiquid)) {
						pos = pos.down();
						state = world.getBlockState(pos);
					}

				if(canSpawnInPosition(schema, world, pos)) {
					boolean generated = StructureGenerator.placeStructureAtPosition(random, schema, Rotation.NONE, (WorldServer) world, pos, true);
					return generated ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
				}
			}

			return EnumActionResult.PASS;
		}

		return EnumActionResult.FAIL;
	}

	public boolean canSpawnInPosition(StructureSchema schema, World world, BlockPos pos) {
		if(schema.generateEverywhere)
			return true;

		if(!schema.dimensionSpawns.isEmpty()) {
			int dim = world.provider.getDimension();
			if(schema.isDimensionSpawnsBlacklist && schema.dimensionSpawns.contains(dim))
				return false;

			if(!schema.isDimensionSpawnsBlacklist && !schema.dimensionSpawns.contains(dim))
				return false;
		}

		Biome biome = world.getBiomeGenForCoords(pos);
		String name = biome.getRegistryName().toString();

		if(schema.isBiomeNameSpawnsBlacklist && !schema.biomeNameSpawns.contains(name))
			return true;
		if(schema.biomeNameSpawns.contains(name))	
			return !schema.isBiomeNameSpawnsBlacklist;

		BiomeDictionary.Type[] types = BiomeDictionary.getTypesForBiome(biome);
		if(schema.isBiomeNameSpawnsBlacklist) {
			for(BiomeDictionary.Type type : types)
				if(schema.biomeTagSpawns.contains(type.name()))
					return false;
			
			return true;
		} else for(BiomeDictionary.Type type : types)
			if(schema.biomeTagSpawns.contains(type.name()))
				return true;

		return false;
	}

}
