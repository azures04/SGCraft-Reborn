/*
  *****************************************************************************************************************************************
  * Source : https://github.com/Tau-ri-Dev/JSGMod-1.18.2/blob/main/Core/src/main/java/dev/tauri/jsgcore/loader/texture/TextureLoader.java *
  *****************************************************************************************************************************************
*/
package fr.azures.mod.sgcraft_reborn.utils.loader.textures;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import fr.azures.mod.sgcraft_reborn.utils.Logging;
import fr.azures.mod.sgcraft_reborn.utils.loader.FolderLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public class TextureLoader {

	private final String modId;
	private final Class modMainClass;
	public final String texturesPath;
	private final Map<ResourceLocation, Texture> LOADED_TEXTURES = new HashMap<>();

	public TextureLoader(String modId, Class modMainClass){
		this.modId = modId;
		this.modMainClass = modMainClass;
		this.texturesPath = "assets/" + modId + "/textures/tesr";
		Logging.info("Created TextureLoader for domain " + modId);
	}
	
	public Texture getTexture(ResourceLocation resourceLocation) {
		return LOADED_TEXTURES.get(resourceLocation);
	}

	public boolean isTextureLoaded(ResourceLocation resourceLocation) {
		return LOADED_TEXTURES.containsKey(resourceLocation);
	}
	
	public void loadTextures(){
		IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		try {
			for (Texture texture : LOADED_TEXTURES.values())
				texture.deleteTexture();

			List<String> texturePaths = FolderLoader.getAllFiles(modMainClass, texturesPath, ".png", ".jpg");

			long start = System.currentTimeMillis();

			Logging.info("Started loading textures for domain " + modId + "...");

			for (String texturePath : texturePaths) {
				texturePath = texturePath.replaceFirst("assets/" + modId + "/", "");

				switch(texturePath){
					default:
						break;
					case "textures/tesr/stargate/horizon/event_horizon_animated_kawoosh.jpg":
					case "textures/tesr/stargate/horizon/event_horizon_animated_kawoosh_unstable.jpg":
					case "textures/tesr/stargate/horizon/event_horizon_animated_unstable.jpg":
					case "textures/tesr/stargate/horizon/event_horizon_animated.jpg":
						Logging.info("Skipping texture " + texturePath + " for domain " + modId);
						continue;
				}

				ResourceLocation resourceLocation = new ResourceLocation(modId, texturePath);
				Resource resource = null;

				try {
					Logging.info("Loading texture: " + texturePath + " for domain " + modId);
					resource = (Resource) resourceManager.getResource(resourceLocation);
					BufferedImage bufferedImage = readBufferedImage(((IResource) resource).getInputStream());
					LOADED_TEXTURES.put(resourceLocation, new Texture(bufferedImage, resourceLocation));
					Logging.info("Texture " + texturePath + " for domain " + modId + " loaded!");
				} catch (IOException e) {
					Logging.error("Failed to load texture " + texturePath);
					e.printStackTrace();
				} finally {
					IOUtils.closeQuietly((Closeable) resource);
				}
			}

			Logging.info("Loaded " + texturePaths.size() + " textures for domain " + modId + " in " + (System.currentTimeMillis() - start) + " ms");
		}
		catch (Exception ignored){}
	}

	public ResourceLocation getTextureResource(String texture) {
		return new ResourceLocation(modId, "textures/tesr/" + texture);
	}

	public ResourceLocation getBlockTexture(BlockState blockState) {
		Minecraft minecraft = Minecraft.getInstance();
		BlockRendererDispatcher ren = minecraft.getBlockRendererDispatcher();
		String blockTexture = ren.getModelForState(blockState).getQuads(blockState, Direction.NORTH, new Random()).get(0).getSprite().getName().toString();
		String domain = "minecraft";
		String path = blockTexture;
		int domainSeparator = blockTexture.indexOf(':');

		if (domainSeparator >= 0) {
			path = blockTexture.substring(domainSeparator + 1);

			if (domainSeparator > 1) {
				domain = blockTexture.substring(0, domainSeparator);
			}
		}

		String resourcePath = "textures/" + path + ".png";  // base path and PNG are hardcoded in Minecraft
		return new ResourceLocation(domain.toLowerCase(), resourcePath);
	}

	// ---- Copy from minecraft 1.12.2 - edited by MineDragonCZ_ ----
	public static BufferedImage readBufferedImage(InputStream imageStream) throws IOException {
		BufferedImage bufferedimage;

		try{
			bufferedimage = ImageIO.read(imageStream);
		}
		finally{
			IOUtils.closeQuietly(imageStream);
		}
		return bufferedimage;
	}
	// ---- END OF COPY ----
}