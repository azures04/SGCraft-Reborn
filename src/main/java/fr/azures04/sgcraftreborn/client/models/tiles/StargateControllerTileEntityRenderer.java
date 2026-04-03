package fr.azures04.sgcraftreborn.client.models.tiles;

import fr.azures04.sgcraftreborn.Constants;
import fr.azures04.sgcraftreborn.client.models.smeg.SMEGLoader;
import fr.azures04.sgcraftreborn.client.models.smeg.SMEGModel;
import fr.azures04.sgcraftreborn.client.models.smeg.SMEGRenderer;
import fr.azures04.sgcraftreborn.registries.blocks.StargateControllerBlock;
import fr.azures04.sgcraftreborn.registries.tiles.StargateControllerTileEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;

public class StargateControllerTileEntityRenderer extends TileEntityRenderer<StargateControllerTileEntity> {

    private final SMEGModel model;

    public StargateControllerTileEntityRenderer() {
        model = SMEGLoader.load(new ResourceLocation(Constants.MOD_ID, "models/block/dhd.smeg"));
    }

    @Override
    public void render(StargateControllerTileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        SMEGRenderer.renderWithRotation(model, x, y, z, tileEntityIn.getBlockState().get(StargateControllerBlock.FACING));
    }
}
