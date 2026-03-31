package fr.azures04.sgcraftreborn.registries.tiles;

import fr.azures04.sgcraftreborn.registries.ModTilesEntities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class StargateBaseTileEntity extends TileEntity {

    public StargateBaseTileEntity() {
        super(ModTilesEntities.STARGATE_BASE_BLOCK);
    }

    public StargateBaseTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

}
