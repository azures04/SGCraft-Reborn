package fr.azures.mod.sgcraft_reborn.utils.loader.textures;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.inventory.Inventory;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Texture extends net.minecraft.client.renderer.texture.Texture {
	private final ResourceLocation resourceLocation;
	public Texture(BufferedImage bufferedImage, ResourceLocation resourceLocation) {
		this.resourceLocation = resourceLocation;
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> {
				uploadTextureImageAllocate(getGlTextureId(), bufferedImage);
			});
		}
		else{
			uploadTextureImageAllocate(getGlTextureId(), bufferedImage);
		}
	}


	public void deleteTexture() {
		deleteGlTexture();
	}

	public void bindTexture() {
		RenderSystem.enableTexture();
		RenderSystem.enableBlend();
		//RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.activeTexture(this.glTextureId);
		//;(this.id, resourceLocation);
		GlStateManager.bindTexture(getGlTextureId());
		//GameRenderer.getPositionTexColorShader();
		Minecraft.getInstance().getAtlasSpriteGetter(new ResourceLocation("textures/atlas/blocks.png")).apply(resourceLocation);
	}

	// ---- Copy from minecraft 1.12.2 - edited by MineDragonCZ_ ----
	private final IntBuffer DATA_BUFFER = ByteBuffer.allocateDirect(4194304 << 2).order(ByteOrder.nativeOrder()).asIntBuffer();
	public void uploadTextureImageAllocate(int textureId, BufferedImage texture){
		TextureUtil.prepareImage(textureId, texture.getWidth(), texture.getHeight());
		GlStateManager.bindTexture(textureId);
		uploadTextureImageSubImpl(texture);
	}
	private void uploadTextureImageSubImpl(BufferedImage texture){
		int i = texture.getWidth();
		int j = texture.getHeight();
		int k = 4194304 / i;
		int[] ant = new int[k * i];

		for (int l = 0; l < i * j; l += i * k)
		{
			int i1 = l / i;
			int j1 = Math.min(k, j - i1);
			int k1 = i * j1;
			texture.getRGB(0, i1, i, j1, ant, 0, i);
			copyToBufferPos(ant, k1);
			GlStateManager.texImage2D(3553, 0, 0, i1, i, j1, 32993, 33639, DATA_BUFFER);
		}
	}
	private void copyToBufferPos(int[] ints, int i) {
		DATA_BUFFER.clear();
		DATA_BUFFER.put(ints, 0, i);
		DATA_BUFFER.position(0).limit(i);
	}
	// ---- END OF COPY ----

	@Override
	public void loadTexture(IResourceManager manager) throws IOException {
		// TODO Auto-generated method stub
		
	}
}