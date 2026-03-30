package fr.azures04.sgcraftreborn.registries.tiles;

import fr.azures04.sgcraftreborn.registries.ModTilesEntities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class StargateControllerTileEntity extends TileEntity {

    public StargateControllerTileEntity() {
        super(ModTilesEntities.STARGATE_CONTROLLER_BLOCK);
    }

    public StargateControllerTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }
}
