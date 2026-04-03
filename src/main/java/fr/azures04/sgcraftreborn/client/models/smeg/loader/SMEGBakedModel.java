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
        if (side != null) return Collections.emptyList();
        VertexFormat format = DefaultVertexFormats.BLOCK;
        List<BakedQuad> quads = new ArrayList<BakedQuad>();
        for (int i = 0; i < model.faces.length; i++) {
            SMEGModel.Face face = model.faces[i];
            Minecraft.getInstance().getTextureManager().bindTexture(model.getTexture(face.texture));
            for (int j = 0; j < face.triangles.length; j++) {
                int[] triangle = face.triangles[j];
                TextureAtlasSprite sprite = spriteGetter.apply(model.getTexture(face.texture));

                UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
                builder.setTexture(sprite);

                for (int k = 0; k < 3; k++) {
                    int vertexIndex = triangle[k];
                    double[] vertex = face.vertices[vertexIndex];
                    builder.put(0, (float) vertex[0], (float) vertex[1], (float) vertex[2], 1.0f);
                    builder.put(1, sprite.getInterpolatedU(vertex[6] * 16), sprite.getInterpolatedV(vertex[7] * 16), 0, 0);
                    builder.put(2, (float) vertex[3], (float) vertex[4], (float) vertex[5], 0);
                }

                double[] last = face.vertices[triangle[2]];
                builder.put(0, (float) last[0], (float) last[1], (float) last[2], 1.0f);
                builder.put(1, sprite.getInterpolatedU(last[6] * 16), sprite.getInterpolatedV(last[7] * 16), 0, 0);
                builder.put(2, (float) last[3], (float) last[4], (float) last[5], 0);

                quads.add(builder.build());
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
        return spriteGetter.apply(model.getTexture(0));
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}
