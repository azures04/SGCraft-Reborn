package fr.azures.mod.sgcraft_reborn.registry.objects.tiles;

import fr.azures.mod.sgcraft_reborn.registry.ModTilesEntities;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class StargateControllerTile extends TileEntity implements ITickableTileEntity {

	public StargateControllerTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
	}
	
	public StargateControllerTile() {
		super(ModTilesEntities.STARGATE_CONTROLLER.get());
	}

	@Override
	public void tick() {
		
	}

}
