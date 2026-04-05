package fr.azures04.sgcraftreborn.client.models.tiles;

import fr.azures04.sgcraftreborn.Constants;
import fr.azures04.sgcraftreborn.client.models.esmeg.ESMEGLoader;
import fr.azures04.sgcraftreborn.client.models.esmeg.ESMEGModel;
import fr.azures04.sgcraftreborn.client.models.esmeg.ESMEGRenderer;
import fr.azures04.sgcraftreborn.registries.blocks.StargateControllerBlock;
import fr.azures04.sgcraftreborn.registries.tiles.StargateControllerTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class StargateControllerTileEntityRenderer extends TileEntityRenderer<StargateControllerTileEntity> {

    private final ESMEGModel model;

    public StargateControllerTileEntityRenderer() {
        model = ESMEGLoader.load(new ResourceLocation(Constants.MOD_ID, "models/block/dhd.esmeg"));
    }

    @Override
    public void render(StargateControllerTileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        IBlockState state = tileEntityIn.getWorld().getBlockState(tileEntityIn.getPos());
        Map<String, String> stateMap = ESMEGModel.BlockStateParser.fromBlockState(state);
        ESMEGRenderer.renderWithRotation(model, x, y, z, tileEntityIn.getBlockState().get(StargateControllerBlock.FACING), stateMap);
    }
}
