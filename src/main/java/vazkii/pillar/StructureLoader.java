/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Pillar Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Pillar
 * 
 * Pillar is Open Source and distributed under the
 * [ADD-LICENSE-HERE]
 * 
 * File Created @ [25/06/2016, 18:22:31 (GMT)]
 */
package vazkii.pillar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import net.minecraftforge.fml.common.FMLLog;
import vazkii.pillar.proxy.CommonProxy;
import vazkii.pillar.schema.StructureSchema;

public final class StructureLoader {

	public static final List<StructureSchema> loadedSchemas = new ArrayList();
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public static void loadStructures() {
		log("Loading structures...");
		File[] files = CommonProxy.pillarDir.listFiles((File f) -> {
			if(!f.getName().endsWith(".json"))
				return false;

			File f1 = new File(CommonProxy.structureDir, getStructureNBTLocation(f.getName()));
			return f1.exists();
		});
		
		loadedSchemas.clear();
		for(File f : files) {
			try {
				StructureSchema schema = gson.<StructureSchema>fromJson(new FileReader(f), new TypeToken<StructureSchema>(){}.getType());
				schema.structureName = getStructureNBTLocation(f.getName()).replaceAll("\\.nbt$", "");
				log("Loaded schema " + schema.structureName);
				if(schema != null)
					loadedSchemas.add(schema);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		log("Finished structure loading. " + loadedSchemas.size() + " Structures loaded.");
	}
	
	public static String getStructureNBTLocation(String jsonFileName) {
		String name = jsonFileName.replaceAll("\\.json$", ".nbt");
		name = name.replaceAll("\\.(?!nbt)", "/");
		return name;
	}
	
	public static String jsonifySchema(StructureSchema schema) {
		return gson.toJson(schema);
	}
	
	public static void log(String m) {
		FMLLog.log(Level.INFO, "[Pillar] %s", m);
	}
	
}
