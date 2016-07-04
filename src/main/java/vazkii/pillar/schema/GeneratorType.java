/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Pillar Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Pillar
 * 
 * Pillar is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * 
 * File Created @ [25/06/2016, 18:10:10 (GMT)]
 */
package vazkii.pillar.schema;

import java.util.Random;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public enum GeneratorType {

	SURFACE(true, GeneratorType::surfacePos),
	UNDERGROUND(true, GeneratorType::undergroundPos),
	UNDERWATER(true, GeneratorType::underwaterPos),
	ABOVE_WATER(true, GeneratorType::aboveWaterPos),
	SKY(false, GeneratorType::skyPos),
	ANYWHERE(false, GeneratorType::anywherePos),
	NONE(false, GeneratorType::disallow);

	private GeneratorType(boolean findLowest, BlockPosProvider provider) {
		this.provider = provider;
		this.findLowest = findLowest;
	}
	
	private BlockPosProvider provider;
	private boolean findLowest;
	
	public BlockPos getGenerationPosition(StructureSchema schema, Random random, World world, BlockPos xzPos) {
		return provider.getGenerationPosition(schema, random, world, xzPos);
	}
	
	public boolean shouldFindLowestBlock() {
		return findLowest;
	}
	
	private static BlockPos surfacePos(StructureSchema schema, Random random, World world, BlockPos xzPos) {
		BlockPos pos = world.getTopSolidOrLiquidBlock(xzPos);
		IBlockState state = world.getBlockState(pos);
		if(pos.getX() == 0 || state.getBlock() instanceof BlockLiquid || !isInYBounds(schema, pos.getY()))
			return null;
		
		return pos; 
	}
	
	private static BlockPos undergroundPos(StructureSchema schema, Random random, World world, BlockPos xzPos) {
		int y = getYFromBounds(schema, random, 0, 60);
		BlockPos pos = new BlockPos(xzPos.getX(), y, xzPos.getZ());
		if(world.canBlockSeeSky(pos))
			return null;
		
		return pos;
	}

	private static BlockPos underwaterPos(StructureSchema schema, Random random, World world, BlockPos xzPos) {
		BlockPos pos = getTopSolidBlock(world, xzPos);
		IBlockState state = world.getBlockState(pos.up());
		if(pos.getX() == 0 || !(state.getBlock() instanceof BlockLiquid))
			return null;
		
		return pos; 
	}
	
	private static BlockPos aboveWaterPos(StructureSchema schema, Random random, World world, BlockPos xzPos) {
		BlockPos pos = getTopLiquidBlock(world, xzPos);
		
		if(pos.getX() == 0 || !isInYBounds(schema, pos.getY()))
			return null;
		
		return pos; 
	}
	
	private static BlockPos skyPos(StructureSchema schema, Random random, World world, BlockPos xzPos) {
		int y = getYFromBounds(schema, random, 128, 256);
		BlockPos pos = new BlockPos(xzPos.getX(), y, xzPos.getZ());
		IBlockState state = world.getBlockState(pos);

		if(!world.canBlockSeeSky(pos) || !state.getBlock().isAir(state, world, pos))
			return null;
		
		return pos;
	}
	
	private static BlockPos anywherePos(StructureSchema schema, Random random, World world, BlockPos xzPos) {
		int y = getYFromBounds(schema, random, 1, 256);
		BlockPos pos = new BlockPos(xzPos.getX(), y, xzPos.getZ());
		IBlockState state = world.getBlockState(pos);

		return pos;
	}
	
	private static BlockPos disallow(StructureSchema schema, Random random, World world, BlockPos xzPos) {
		return null;
	}
	
	private static boolean isInYBounds(StructureSchema schema, int y) {
		if(schema.maxY > -1 && y > schema.maxY)
			return false;
		if(schema.minY > -1 && y < schema.minY)
			return false;
		
		return true;
	}
	
	private static int getYFromBounds(StructureSchema schema, Random rand, int defaultMin, int defaultMax) {
		int maxY = schema.maxY;
		int minY = schema.minY;
		
		if(maxY < 0)
			maxY = defaultMax;
		if(minY < 0)
			minY = defaultMin;
		
		if(minY > maxY) {
			int i = maxY;
			maxY = minY;
			minY = i;
		}
		
		int diff = maxY - minY;
		return rand.nextInt(diff) + minY;
	}
	
    private static BlockPos getTopSolidBlock(World world, BlockPos pos) {
        Chunk chunk = world.getChunkFromBlockCoords(pos);
        BlockPos blockpos;
        BlockPos blockpos1;

        for(blockpos = new BlockPos(pos.getX(), chunk.getTopFilledSegment() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
            blockpos1 = blockpos.down();
            IBlockState state = chunk.getBlockState(blockpos1);

            if(state.isOpaqueCube() && !state.getBlock().isLeaves(state, world, blockpos1) && !state.getBlock().isFoliage(world, blockpos1))
                break;
        }

        return blockpos;
    }
	
    private static BlockPos getTopLiquidBlock(World world, BlockPos pos) {
        Chunk chunk = world.getChunkFromBlockCoords(pos);
        BlockPos blockpos;
        BlockPos blockpos1;

        for(blockpos = new BlockPos(pos.getX(), chunk.getTopFilledSegment() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
            blockpos1 = blockpos.down();
            IBlockState state = chunk.getBlockState(blockpos1);

            if(state.getBlock() instanceof BlockLiquid)
                break;
        }

        return blockpos;
    }
    
	public static interface BlockPosProvider {
		public BlockPos getGenerationPosition(StructureSchema schema, Random random, World world, BlockPos xzPos);
	}
	
}
