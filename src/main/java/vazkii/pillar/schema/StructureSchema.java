/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Pillar Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Pillar
 * 
 * Pillar is Open Source and distributed under the
 * [ADD-LICENSE-HERE]
 * 
 * File Created @ [25/06/2016, 18:08:37 (GMT)]
 */
package vazkii.pillar.schema;

import java.util.List;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import vazkii.pillar.StructureLoader;

public final class StructureSchema {

	public transient String structureName;

	public GeneratorType generatorType;
	public int maxY, minY;

	public int offsetX, offsetY, offsetZ;
	public String mirrorType;
	public String rotation;
	public boolean ignoreEntities;

	public List<Integer> dimensionSpawns;
	public List<String> biomeNameSpawns;
	public List<String> biomeTagSpawns;

	public boolean isDimensionSpawnsBlacklist;
	public boolean isBiomeNameSpawnsBlacklist;
	public boolean isBiomeTagSpawnsBlacklist;
	public boolean generateEverywhere;

	public float integrity, decay;
	public int rarity;

	public String filling;
	public int fillingMetadata;
	public FillingType fillingType;

	@Override
	public String toString() {
		return StructureLoader.jsonifySchema(this);
	}

	public Mirror getMirrorType() {
		switch(mirrorType) {
		case "mirror_left_right":
		case "LEFT_RIGHT":
			return Mirror.LEFT_RIGHT;
		case "mirror_front_back":
		case "FRONT_BACK":
			return Mirror.FRONT_BACK;
		}
		return Mirror.NONE;
	}

	public Rotation getRotation() {
		switch(rotation) {
		case "90": 
		case "-270":
			return Rotation.CLOCKWISE_90;
		case "180":
		case "-180":
			return Rotation.CLOCKWISE_180;
		case "270":
		case "-90":
			return Rotation.COUNTERCLOCKWISE_90;
		}
		return Rotation.NONE;
	}
}
