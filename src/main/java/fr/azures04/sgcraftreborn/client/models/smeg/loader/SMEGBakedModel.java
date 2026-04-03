package fr.azures04.sgcraftreborn.client.models.smeg.loader;

import fr.azures04.sgcraftreborn.client.models.smeg.SMEGModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class SMEGBakedModel implements IBakedModel {

    private final SMEGModel model;
    private final Function<ResourceLocation, TextureAtlasSprite> spriteGetter;

    public SMEGBakedModel(SMEGModel model, Function<ResourceLocation, TextureAtlasSprite> spriteGetter) {
        this.model = model;
        this.spriteGetter = spriteGetter;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, Random rand) {
        VertexFormat format = DefaultVertexFormats.BLOCK;
        List<BakedQuad> quads = new ArrayList<BakedQuad>();
        for (int i = 0; i < model.faces.length; i++) {
            SMEGModel.Face face = model.faces[i];
            Minecraft.getInstance().getTextureManager().bindTexture(model.getTexture(face.texture));
            for (int j = 0; j < face.triangles.length; j++) {
                int[] triangle = face.triangles[j];
                for (int k = 0; k < 3; k++) {
                    int vertexIndex = triangle[k];
                    double[] vertex = face.vertices[vertexIndex];
                    UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
                    BakedQuad quad = builder.build();
                    builder.put(0, (float) vertex[0], (float) vertex[1], (float) vertex[2], 1.0f);
                    builder.put(1, (float) vertex[6], (float) vertex[7]);
                    builder.put(2, (float) vertex[3], (float) vertex[4], (float) vertex[5], 0);
                    builder.put(2, (float) vertex[3], (float) vertex[4], (float) vertex[5], 0);
                    quads.add(quad);
                }
            }
        }
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return null;
    }
}
