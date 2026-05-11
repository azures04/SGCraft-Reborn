package fr.azures04.sgcraftreborn.common.registries.tiles;

import fr.azures04.sgcraftreborn.common.integrations.CCTIntegration;
import fr.azures04.sgcraftreborn.common.registries.ModTilesEntities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ComputerCraftInterfaceTileEntity extends TileEntity {

    private LazyOptional<?> peripheralCap = LazyOptional.empty();

    public ComputerCraftInterfaceTileEntity(TileEntityType<?> tileEntityType) {
        super(tileEntityType);
    }

    public ComputerCraftInterfaceTileEntity() {
        super(ModTilesEntities.COMPUTER_CRAFT_INTERFACE_BLOCK);
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

    public void notifyComputerCraft() {
        if (world != null && !world.isRemote) {
            world.notifyNeighborsOfStateChange(pos, this.getBlockState().getBlock());
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!world.isRemote && ModList.get().isLoaded("computercraft")) {
            StargateBaseTileEntity gate = findStargate();
            if (gate != null) {
                this.peripheralCap = CCTIntegration.createPeripheral(gate);
            }
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (ModList.get().isLoaded("computercraft") && cap == CCTIntegration.CAPABILITY_PERIPHERAL) {
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        peripheralCap.invalidate();
    }

}
