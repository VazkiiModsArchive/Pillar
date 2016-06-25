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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;
import vazkii.pillar.proxy.CommonProxy;
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
			boolean did = generateStructure(schema, random, world, chunkX, chunkZ);
			
			if(did)
				structuresGenerated++;
			if(structuresGenerated >= CommonProxy.maxStructuresInOneChunk)
				break;
		}
	}
	
	public boolean generateStructure(StructureSchema schema, Random random, World world, int chunkX, int chunkZ) {
		int rarity = (int) (schema.rarity * CommonProxy.rarityMultiplier);
		if(rarity > 0 && random.nextInt(rarity) == 0) {
			int x = chunkX * 16 + random.nextInt(16);
			int z = chunkZ * 16 + random.nextInt(16);
			BlockPos xzPos = new BlockPos(x, 0, z);
			BlockPos pos = schema.generatorType.getGenerationPosition(schema, random, world, xzPos);

			if(pos != null) {
				IBlockState state = world.getBlockState(pos);
				while(state.getBlock().isReplaceable(world, pos) && !(state.getBlock() instanceof BlockLiquid)) {
					pos = pos.down();
					state = world.getBlockState(pos);
				}
				
				if(canSpawnInPosition(schema, world, pos))
					return StructureGenerator.placeStructureAtPosition(random, schema, (WorldServer) world, pos);
			}
		}
		
		return false;
	}
	
	public boolean canSpawnInPosition(StructureSchema schema, World world, BlockPos pos) {
		if(schema.generateEverywhere)
			return true;
		
		if(!schema.dimensionSpawns.isEmpty()) {
			int dim = world.provider.getDimension();
			return schema.dimensionSpawns.contains(dim) == schema.isDimensionSpawnsBlacklist;
		}
		
		Biome biome = world.getBiomeGenForCoords(pos);
		String name = biome.getRegistryName().toString();
		if(schema.biomeNameSpawns.contains(name))
			return !schema.isBiomeNameSpawnsBlacklist;
		
		BiomeDictionary.Type[] types = BiomeDictionary.getTypesForBiome(biome);
		for(BiomeDictionary.Type type : types) {
			if(schema.biomeTagSpawns.contains(type.name()))
				return !schema.isBiomeTagSpawnsBlacklist;
		}
		
		return false;
	}
	
}
