package fr.azures04.sgcraftreborn.client.models.smeg.loader;

import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.resource.IResourceType;

import java.util.function.Predicate;

public class SMEGModelLoader implements ICustomModelLoader {


    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        ICustomModelLoader.super.onResourceManagerReload(resourceManager, resourcePredicate);
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getPath().endsWith(".smeg");
    }

    @Override
    public IUnbakedModel loadModel(ResourceLocation modelLocation) throws Exception {
        return null;
    }
}
