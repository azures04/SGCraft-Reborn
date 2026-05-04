package fr.azures04.sgcraftreborn.client.models.tiles.items;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.client.models.esmeg.ESMEGLoader;
import fr.azures04.sgcraftreborn.client.models.esmeg.ESMEGModel;
import fr.azures04.sgcraftreborn.client.models.esmeg.ESMEGRenderer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public class StargateControllerISTER extends ItemStackTileEntityRenderer {

    private ESMEGModel model;

    public StargateControllerISTER() {
        model = ESMEGLoader.load(new ResourceLocation(Constants.MOD_ID, "models/block/dhd.esmeg"));
    }

    @Override
    public void renderByItem(ItemStack stack) {
        ESMEGRenderer.renderWithRotation(model, 0, 0, 0, Direction.NORTH);
    }
}