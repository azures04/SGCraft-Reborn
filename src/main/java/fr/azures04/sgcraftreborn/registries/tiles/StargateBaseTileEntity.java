package fr.azures04.sgcraftreborn.registries.tiles;

import fr.azures04.sgcraftreborn.SGCraftReborn;
import fr.azures04.sgcraftreborn.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.registries.ModTilesEntities;
import fr.azures04.sgcraftreborn.registries.blocks.StargateBaseBlock;
import fr.azures04.sgcraftreborn.registries.structures.StargateStructure;
import fr.azures04.sgcraftreborn.registries.tiles.states.StargateIrisState;
import fr.azures04.sgcraftreborn.registries.tiles.states.StargateVortexState;
import fr.azures04.sgcraftreborn.registries.world.data.StargateWorldData;
import fr.azures04.sgcraftreborn.util.math.ExtendedPos;
import fr.azures04.sgcraftreborn.registries.world.StargateAddressing;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import java.util.Objects;

public class StargateBaseTileEntity extends TileEntity {

    private boolean isMerged;
    private boolean isInitiator;
    private ExtendedPos connectedLoc;
    private BlockPos controllerPos;
    private String dialledAddress = "";
    private String address = "";
    private double energyInBuffer;
    private int numEngagedChevrons;
    private boolean hasChevronUpgrade;
    private boolean hasIrisUpgrade;
    private StargateIrisState irisState = StargateIrisState.OPEN;
    private int irisPhase;
    private int lastIrisPhase;
    private StargateVortexState vortexState = StargateVortexState.IDLE;
    private double ringAngle;
    private double lastRingAngle;
    private double targetRingAngle;
    private int timeout;
    static final int DIALLING_TIME = 40;
    static final int INTER_DIALLING_TIME = 10;
    static final int TRANSIENT_DURATION = 20;
    static final int DISCONNECT_TIME = 30;

    public StargateBaseTileEntity() {
        super(ModTilesEntities.STARGATE_BASE_BLOCK);
    }

    public StargateBaseTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void read(NBTTagCompound compound) {
        super.read(compound);
        isMerged = compound.getBoolean("isMerged");
        isInitiator = compound.getBoolean("isInitiator");
        if (compound.contains("connectedX", 3)) {
            connectedLoc = new ExtendedPos(
                compound.getInt("connectedX"),
                compound.getInt("connectedY"),
                compound.getInt("connectedZ"),
                compound.getInt("connectedD")
            );
        }
        if (compound.contains("controllerX", 3)) {
            controllerPos = new BlockPos(
                compound.getInt("controllerX"),
                compound.getInt("controllerY"),
                compound.getInt("controllerZ")
            );
        }
        dialledAddress = compound.getString("dialledAddress");
        energyInBuffer = compound.getDouble("energyInBuffer");
        numEngagedChevrons = compound.getInt("numEngagedChevrons");
        hasChevronUpgrade = compound.getBoolean("hasChevronUpgrade");
        hasIrisUpgrade = compound.getBoolean("hasIrisUpgrade");
        irisState = StargateIrisState.values()[compound.getInt("irisState")];
        irisPhase = compound.getInt("irisPhase");
        lastIrisPhase = compound.getInt("lastIrisPhase");
        vortexState = StargateVortexState.values()[compound.getInt("vortexState")];
        ringAngle = compound.getDouble("ringAngle");
        lastRingAngle = compound.getDouble("lastRingAngle");
        targetRingAngle = compound.getDouble("targetRingAngle");
        address = compound.getString("address");
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        compound.setBoolean("isMerged", isMerged);
        compound.setBoolean("isInitiator", isInitiator);
        if (connectedLoc != null) {
            compound.setInt("connectedX", connectedLoc.getX());
            compound.setInt("connectedY", connectedLoc.getY());
            compound.setInt("connectedZ", connectedLoc.getZ());
            compound.setInt("connectedD", connectedLoc.getDimension());
        }
        if (controllerPos != null) {
            compound.setInt("controllerX", controllerPos.getX());
            compound.setInt("controllerY", controllerPos.getY());
            compound.setInt("controllerZ", controllerPos.getZ());
        }
        compound.setString("dialledAddress", dialledAddress);
        compound.setDouble("energyInBuffer", energyInBuffer);
        compound.setInt("numEngagedChevrons", numEngagedChevrons);
        compound.setBoolean("hasChevronUpgrade", hasChevronUpgrade);
        compound.setBoolean("hasIrisUpgrade", hasIrisUpgrade);
        compound.setInt("irisState", irisState.ordinal());
        compound.setInt("irisPhase", irisPhase);
        compound.setInt("lastIrisPhase", lastIrisPhase);
        compound.setInt("vortexState", vortexState.ordinal());
        compound.setDouble("ringAngle", ringAngle);
        compound.setDouble("lastRingAngle", lastRingAngle);
        compound.setDouble("targetRingAngle", targetRingAngle);
        compound.setString("address", address);
        return super.write(compound);
    }

    public void setMerged(boolean merged) {
        if (this.isMerged != merged) {
            this.isMerged = merged;

            EnumFacing facing = getBlockState().get(StargateBaseBlock.FACING);

            if (!world.isRemote) {
                if (merged) {
                    StargateStructure.hideStructure(world, pos, facing);
                    this.notifyNearbyControllers();
                    if (SGCraftRebornConfig.LOG_STARGATE_EVENTS.get()) {
                        SGCraftReborn.LOGGER.info(String.format("STARGATE ADDED DIM: %d, X: %d Y: %d Z: %d", world.dimension.getType().getId(), pos.getX(), pos.getY(), pos.getZ()));
                    }
                } else {
                    StargateStructure.showStructure(world, pos, facing);
                    if (SGCraftRebornConfig.LOG_STARGATE_EVENTS.get()) {
                        SGCraftReborn.LOGGER.info(String.format("STARGATE REMOVED DIM: %d, X: %d Y: %d Z: %d", world.dimension.getType().getId(), pos.getX(), pos.getY(), pos.getZ()));
                    }
                    if (controllerPos != null) {
                        TileEntity te = world.getTileEntity(controllerPos);
                        if (te instanceof StargateControllerTileEntity) {
                            ((StargateControllerTileEntity) te).unlink();
                        }
                        controllerPos = null;
                        markDirty();
                    }
                }
            }
            this.markDirty();
        }
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
        markDirty();
    }

    public void setInitiator(boolean initiator) {
        isInitiator = initiator;
        markDirty();
    }

    public void setLocationPos(ExtendedPos location) {
        connectedLoc = location;
        markDirty();
    }

    public void setDialledAddress(String dialledAddress) {
        this.dialledAddress = dialledAddress;
        markDirty();
    }

    public void setEnergyInBuffer(double energyInBuffer) {
        this.energyInBuffer = energyInBuffer;
        markDirty();
    }

    public void setNumEngagedChevrons(int numEngagedChevrons) {
        this.numEngagedChevrons = numEngagedChevrons;
        markDirty();
    }

    public void setHasChevronUpgrade(boolean hasChevronUpgrade) {
        this.hasChevronUpgrade = hasChevronUpgrade;
        markDirty();
    }

    public void setHasIrisUpgrade(boolean hasIrisUpgrade) {
        this.hasIrisUpgrade = hasIrisUpgrade;
        markDirty();
    }

    public void setIrisState(StargateIrisState irisState) {
        this.irisState = irisState;
        markDirty();
    }

    public void setIrisPhase(int irisPhase) {
        this.irisPhase = irisPhase;
        markDirty();
    }

    public void setLastIrisPhase(int lastIrisPhase) {
        this.lastIrisPhase = lastIrisPhase;
        markDirty();
    }

    public void setVortexState(StargateVortexState vortexState) {
        this.vortexState = vortexState;
        markDirty();
    }

    public void setRingAngle(double ringAngle) {
        this.ringAngle = ringAngle;
        markDirty();
    }

    public void setLastRingAngle(double lastRingAngle) {
        this.lastRingAngle = lastRingAngle;
        markDirty();
    }

    public void setTargetRingAngle(double targetRingAngle) {
        this.targetRingAngle = targetRingAngle;
        markDirty();
    }

    public boolean isMerged() {
        return isMerged;
    }

    public boolean isInitiator() {
        return isInitiator;
    }

    public ExtendedPos getConnectedLoc() {
        return connectedLoc;
    }

    public String getDialledAddress() {
        return dialledAddress;
    }

    public double getEnergyInBuffer() {
        return energyInBuffer;
    }

    public int getNumEngagedChevrons() {
        return numEngagedChevrons;
    }

    public boolean isHasChevronUpgrade() {
        return hasChevronUpgrade;
    }

    public boolean isHasIrisUpgrade() {
        return hasIrisUpgrade;
    }

    public StargateIrisState getIrisState() {
        return irisState;
    }

    public int getIrisPhase() {
        return irisPhase;
    }

    public int getLastIrisPhase() {
        return lastIrisPhase;
    }

    public StargateVortexState getVortexState() {
        return vortexState;
    }

    public double getRingAngle() {
        return ringAngle;
    }

    public double getLastRingAngle() {
        return lastRingAngle;
    }

    public double getTargetRingAngle() {
        return targetRingAngle;
    }

    public String getAddress() {
        if (address == null || address.trim().isEmpty()) {
            address = StargateAddressing.generateAddress();
            markDirty();
        }
        return address;
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    @Override
    public void onLoad() {
        if (!world.isRemote) {
            StargateWorldData.get(world).register(
                getAddress(),
                new ExtendedPos(pos, world.getDimension().getType().getId())
            );
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (!world.isRemote) {
            StargateWorldData.get(world).unregister(getAddress());
        }
    }

    private void notifyNearbyControllers() {
        double rX = SGCraftRebornConfig.LINK_RANGE_X.get() * 2.0;
        double rY = SGCraftRebornConfig.LINK_RANGE_Y.get() * 2.0;
        double rZ = SGCraftRebornConfig.LINK_RANGE_Z.get() * 2.0;

        AxisAlignedBB searchBox = new AxisAlignedBB(pos).grow(rX, rY, rZ);

        for (TileEntity te : world.loadedTileEntityList) {
            if (te instanceof StargateControllerTileEntity) {
                BlockPos tePos = te.getPos();
                if (searchBox.contains(tePos.getX(), tePos.getY(), tePos.getZ())) {
                    ((StargateControllerTileEntity) te).getLinkedStargateTE(world);
                }
            }
        }
    }

}
