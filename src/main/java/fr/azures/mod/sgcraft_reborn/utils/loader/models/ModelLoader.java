/*
  *************************************************************************************************************************************
  * Source : https://github.com/Tau-ri-Dev/JSGMod-1.18.2/blob/main/Core/src/main/java/dev/tauri/jsgcore/loader/model/ModelLoader.java *
  *************************************************************************************************************************************
*/
package fr.azures.mod.sgcraft_reborn.utils.loader.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.azures.mod.sgcraft_reborn.utils.Logging;
import fr.azures.mod.sgcraft_reborn.utils.loader.FolderLoader;
import net.minecraft.util.ResourceLocation;

public class ModelLoader {

	private final String modId;
	private final Class modMainClass;
	public final String modelsPath;
	private final Map<ResourceLocation, OBJModel> LOADED_MODELS = new HashMap<>();

	public ModelLoader(String modId, Class modMainClass){
		this.modId = modId;
		this.modMainClass = modMainClass;
		this.modelsPath = "assets/" + modId + "/models/tesr";
		Logging.info("Created ModelLoader for domain " + modId);
	}
	
	public OBJModel getModel(ResourceLocation resourceLocation) {
		return LOADED_MODELS.get(resourceLocation);
	}

	public void loadModels() {
		try {
			LOADED_MODELS.clear();

			List<String> modelPaths = FolderLoader.getAllFiles(modMainClass, modelsPath, ".obj");

			long start = System.currentTimeMillis();

			Logging.info("Started loading models for domain " + modId + "...");
			for (String modelPath : modelPaths) {
				Logging.info("Loading model: " + modelPath + " for domain " + modId);
				String modelResourcePath = modelPath.replaceFirst("assets/" + modId + "/", "");
				LOADED_MODELS.put(new ResourceLocation(modId, modelResourcePath), OBJLoader.loadModel(modelPath, modMainClass));
				Logging.info("Model " + modelPath + " for domain " + modId + " loaded!");
			}

			Logging.info("Loaded " + modelPaths.size() + " models for domain " + modId + " in " + (System.currentTimeMillis() - start) + " ms");
		}
		catch (Exception e){
			Logging.error("Error while loading models for mod " + modMainClass.toString());
			e.printStackTrace();
		}
	}
	
	public ResourceLocation getModelResource(String model) {
		return new ResourceLocation(modId, "models/tesr/" + model);
	}
}