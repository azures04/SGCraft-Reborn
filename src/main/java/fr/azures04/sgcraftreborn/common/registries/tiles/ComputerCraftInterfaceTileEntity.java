package fr.azures04.sgcraftreborn.common.registries.tiles;

import fr.azures04.sgcraftreborn.common.api.StargateAbstractAPI;
import fr.azures04.sgcraftreborn.common.registries.ModTilesEntities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;

public class ComputerCraftInterfaceTileEntity extends TileEntity {

    private StargateAbstractAPI peripheralAdapter;

    public ComputerCraftInterfaceTileEntity(TileEntityType<?> tileEntityType) {
        super(tileEntityType);
    }

    public ComputerCraftInterfaceTileEntity() {
        super(ModTilesEntities.COMPUTER_CRAFT_INTERFACE_BLOCK);
    }

    public StargateAbstractAPI getPeripheralAdapter() {
        return peripheralAdapter;
    }

    public void setPeripheralAdapter(StargateAbstractAPI peripheralAdapter) {
        this.peripheralAdapter = peripheralAdapter;
    }

    public StargateBaseTileEntity findStargate() {
        BlockPos minPos = pos.add(-3, 0, -3);
        BlockPos maxPos = pos.add(3, 1, 3);

        for (BlockPos scanPos : BlockPos.getAllInBoxMutable(minPos, maxPos)) {
            TileEntity te = world.getTileEntity(scanPos);
            if (te instanceof StargateBaseTileEntity) {
                return (StargateBaseTileEntity) te;
            }
        }
        return null;
    }

    @Override
    public void remove() {
        if (world != null && !world.isRemote && peripheralAdapter != null) {
            StargateBaseTileEntity base = findStargate();

            if (base != null) {
                base.removeComputerAdapter(peripheralAdapter);
            }
        }

        super.remove();

        if (peripheralAdapter != null) {
            peripheralAdapter = null;
        }
    }
    public void notifyComputerCraft() {
        if (world != null && !world.isRemote) {
            world.notifyNeighborsOfStateChange(pos, this.getBlockState().getBlock());
        }
    }

}
