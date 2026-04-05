package fr.azures04.sgcraftreborn.registries.tiles;

import fr.azures04.sgcraftreborn.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.registries.ModTilesEntities;
import fr.azures04.sgcraftreborn.util.math.ExtendedPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateControllerTileEntity extends TileEntity {

    private double fuelLevel;
    private ExtendedPos linkedStargate;

    public StargateControllerTileEntity() {
        super(ModTilesEntities.STARGATE_CONTROLLER_BLOCK);
    }

    public StargateControllerTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public ExtendedPos getLinkedStargate() {
        return linkedStargate;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(double fuelLevel) {
        this.fuelLevel = fuelLevel;
        markDirty();
    }

    public void setLinkedStargate(ExtendedPos linkedStargate) {
        this.linkedStargate = linkedStargate;
        markDirty();
    }

    private StargateBaseTileEntity searchNearbyStargate(World world) {
        int rangeX = SGCraftRebornConfig.LINK_RANGE_X.get();
        int rangeY = SGCraftRebornConfig.LINK_RANGE_Y.get();
        int rangeZ = SGCraftRebornConfig.LINK_RANGE_Z.get();

        for (int y = -rangeY; y <= rangeY; y++) {
            for (int x = -rangeX; x <= rangeX; x++) {
                for (int z = -rangeZ; z <= rangeZ; z++) {
                    BlockPos checkPos = pos.add(x, y, z);
                    TileEntity te = world.getTileEntity(checkPos);
                    if (te instanceof StargateBaseTileEntity) {
                        StargateBaseTileEntity gate = (StargateBaseTileEntity) te;
                        if (gate.isMerged()) return gate;
                    }
                }
            }
        }
        return null;
    }

    private void linkToStargate(StargateBaseTileEntity gate, World world) {
        linkedStargate = new ExtendedPos(gate.getPos(), world.getDimension().getType().getId());
        gate.setControllerPos(new ExtendedPos(pos, world.getDimension().getType().getId()));
        markDirty();
    }

    public StargateBaseTileEntity getLinkedStargateTE(World world) {
        if (linkedStargate != null) {
            TileEntity te = world.getTileEntity(linkedStargate);
            if (te instanceof StargateBaseTileEntity && ((StargateBaseTileEntity) te).isMerged()) {
                return (StargateBaseTileEntity) te;
            }
            unlink();
        }
        StargateBaseTileEntity gate = searchNearbyStargate(world);
        if (gate != null) linkToStargate(gate, world);
        return gate;
    }

    public void unlink() {
        linkedStargate = null;
        markDirty();
    }

    @Override
    public void read(NBTTagCompound compound) {
        super.read(compound);
        fuelLevel = compound.getDouble("fuelLevel");
        if (compound.contains("connectedX", 3)) {
            linkedStargate = new ExtendedPos(
                compound.getInt("connectedX"),
                compound.getInt("connectedY"),
                compound.getInt("connectedZ"),
                compound.getInt("connectedD")
            );
        }
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        compound.setDouble("fuelLevel", fuelLevel);
        if (linkedStargate != null) {
            compound.setInt("connectedX", linkedStargate.getX());
            compound.setInt("connectedY", linkedStargate.getY());
            compound.setInt("connectedZ", linkedStargate.getZ());
            compound.setInt("connectedD", linkedStargate.getDimension());
        }
        return super.write(compound);
    }
}
