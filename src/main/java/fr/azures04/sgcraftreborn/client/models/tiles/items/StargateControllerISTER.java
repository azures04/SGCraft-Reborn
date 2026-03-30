package fr.azures04.sgcraftreborn.client.models.tiles.items;

import fr.azures04.sgcraftreborn.Constants;
import fr.azures04.sgcraftreborn.client.models.SMEGLoader;
import fr.azures04.sgcraftreborn.client.models.SMEGModel;
import fr.azures04.sgcraftreborn.client.models.SMEGRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class StargateControllerISTER extends TileEntityItemStackRenderer {

    private SMEGModel model;

    public StargateControllerISTER() {
        model = SMEGLoader.load(new ResourceLocation(Constants.MOD_ID, "models/block/dhd.smeg"));
    }

    @Override
    public void renderByItem(ItemStack stack) {
        SMEGRenderer.renderWithRotation(model, 0, 0, 0, EnumFacing.NORTH);
    }
}