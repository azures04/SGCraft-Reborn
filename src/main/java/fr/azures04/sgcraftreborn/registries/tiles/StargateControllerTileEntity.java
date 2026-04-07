package fr.azures04.sgcraftreborn.registries.tiles;

import fr.azures04.sgcraftreborn.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.registries.ModTilesEntities;
import fr.azures04.sgcraftreborn.registries.blocks.StargateControllerBlock;
import fr.azures04.sgcraftreborn.registries.blocks.states.StargateControllerStatus;
import fr.azures04.sgcraftreborn.registries.world.StargateAddressing;
import fr.azures04.sgcraftreborn.registries.world.data.StargateWorldData;
import fr.azures04.sgcraftreborn.util.math.ExtendedPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
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
        AxisAlignedBB box = new AxisAlignedBB(pos).grow(
                SGCraftRebornConfig.LINK_RANGE_X.get(),
                SGCraftRebornConfig.LINK_RANGE_Y.get(),
                SGCraftRebornConfig.LINK_RANGE_Z.get()
        );

        for (BlockPos checkPos : BlockPos.getAllInBoxMutable(
                (int)box.minX, (int)box.minY, (int)box.minZ,
                (int)box.maxX, (int)box.maxY, (int)box.maxZ)) {

            TileEntity te = world.getTileEntity(checkPos);
            if (te instanceof StargateBaseTileEntity) {
                StargateBaseTileEntity gate = (StargateBaseTileEntity) te;
                if (gate.isMerged()) return gate;
            }
        }
        return null;
    }

    private void linkToStargate(StargateBaseTileEntity gate, World world) {
        if (gate.getControllerPos() != null) {
            TileEntity existing = world.getTileEntity(gate.getControllerPos());
            if (existing instanceof StargateControllerTileEntity) {
                return;
            }
        }
        linkedStargate = new ExtendedPos(gate.getPos(), world.getDimension().getType().getId());
        gate.setControllerPos(new ExtendedPos(pos, world.getDimension().getType().getId()));
        IBlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(StargateControllerBlock.STATUS, StargateControllerStatus.LINKED), 3);
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
        IBlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(StargateControllerBlock.STATUS, StargateControllerStatus.UNLINKED), 3);
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

    @Override
    public void onLoad() {
        if (!world.isRemote) {
            getLinkedStargateTE(world);
        }
    }
    public void onDial(String targetAddress) {
        if (world.isRemote) return;
        StargateWorldData data = StargateWorldData.get(this.world);

        ExtendedPos destination = data.findStargate(targetAddress);

        if (destination != null) {
            StargateBaseTileEntity localGate = this.getLinkedStargateTE(world);

            if (localGate != null) {
                localGate.setDialledAddress(targetAddress);
            }
        } else {
            throw StargateAddressing.StargateAddressingException.INVALID_ADDRESS;
        }
    }

}
