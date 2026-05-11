package fr.azures04.sgcraftreborn.client.models.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.azures04.sgcraftreborn.client.models.esmeg.ESMEGRenderer;
import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.client.models.esmeg.ESMEGLoader;
import fr.azures04.sgcraftreborn.client.models.esmeg.ESMEGModel;
import fr.azures04.sgcraftreborn.common.registries.blocks.StargateControllerBlock;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateControllerTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class StargateControllerTileEntityRenderer extends TileEntityRenderer<StargateControllerTileEntity> {

    private final ESMEGModel model;

    public StargateControllerTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        model = ESMEGLoader.load(new ResourceLocation(Constants.MOD_ID, "models/block/dhd.esmeg"));
    }


    @Override
    public void render(StargateControllerTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        BlockState state = tileEntityIn.getBlockState();
        Map<String, String> stateMap = ESMEGModel.BlockStateParser.fromBlockState(state);
        ESMEGRenderer.renderWithRotation(this.model, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, state.get(StargateControllerBlock.FACING), stateMap);
    }



}
