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
		
		if(schema.rotation == null)
			settings.setRotation(Rotation.values()[rand.nextInt(Rotation.values().length)]);
		else settings.setRotation(schema.rotation);
		
		settings.setIgnoreEntities(schema.ignoreEntities);
		settings.setChunk((ChunkPos) null);
		settings.setReplacedBlock((Block) null);
		settings.setIgnoreStructureBlock(false);

		settings.func_189946_a(MathHelper.clamp_float(schema.integrity, 0.0F, 1.0F));

		BlockPos finalPos = pos.add(schema.offsetX, schema.offsetY, schema.offsetZ);
		template.addBlocksToWorldChunk(world, finalPos, settings);
		
		return true;
	}


}
