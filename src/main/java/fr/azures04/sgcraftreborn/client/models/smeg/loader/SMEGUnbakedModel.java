package fr.azures04.sgcraftreborn.client.models.smeg.loader;

import fr.azures04.sgcraftreborn.client.models.smeg.SMEGModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class SMEGUnbakedModel implements IUnbakedModel {

    private final SMEGModel model;

    private SMEGUnbakedModel(SMEGModel model) {
        this.model = model;
    }

    @Override
    public Collection<ResourceLocation> getOverrideLocations() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ResourceLocation> getTextures(Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors) {
        return Arrays.asList(model.getTextures());
    }

    @Nullable
    @Override
    public IBakedModel bake(Function<ResourceLocation, IUnbakedModel> modelGetter, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, IModelState state, boolean uvlock, VertexFormat format) {
        return new SMEGBakedModel(model, spriteGetter);
    }
}
