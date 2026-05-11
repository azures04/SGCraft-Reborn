package fr.azures04.sgcraftreborn.client.registries;

import fr.azures04.sgcraftreborn.client.models.tiles.StargateBaseTileEntityRenderer;
import fr.azures04.sgcraftreborn.client.models.tiles.StargateControllerTileEntityRenderer;
import fr.azures04.sgcraftreborn.common.registries.ModBlocks;
import fr.azures04.sgcraftreborn.common.registries.ModTilesEntities;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ModRender {

    public static void registerRenderTypes() {
        RenderTypeLookup.setRenderLayer(ModBlocks.STARGATE_BASE.getBlock(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.STARGATE_CHEVRON.getBlock(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.STARGATE_RING.getBlock(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.NAQUADAH_ORE.getBlock(), RenderType.getCutout());
    }

    public static void registerCustomTilesRenderer() {
        ClientRegistry.bindTileEntityRenderer(ModTilesEntities.STARGATE_CONTROLLER_BLOCK, StargateControllerTileEntityRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ModTilesEntities.STARGATE_BASE_BLOCK, StargateBaseTileEntityRenderer::new);
    }

}
