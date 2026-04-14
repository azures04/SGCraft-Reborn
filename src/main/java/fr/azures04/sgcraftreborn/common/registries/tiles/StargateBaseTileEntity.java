package fr.azures04.sgcraftreborn.common.registries.tiles;

import fr.azures04.sgcraftreborn.SGCraftReborn;
import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.common.registries.ModTilesEntities;
import fr.azures04.sgcraftreborn.common.registries.blocks.StargateBaseBlock;
import fr.azures04.sgcraftreborn.common.registries.structures.StargateStructure;
import fr.azures04.sgcraftreborn.common.registries.tiles.states.StargateIrisState;
import fr.azures04.sgcraftreborn.common.registries.tiles.states.StargateVortexState;
import fr.azures04.sgcraftreborn.common.world.StargateTeleporter;
import fr.azures04.sgcraftreborn.common.world.data.StargateWorldData;
import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import fr.azures04.sgcraftreborn.common.world.StargateAddressing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.Level;
import java.util.List;
import java.util.Objects;

public class StargateBaseTileEntity extends TileEntity implements ITickable {

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
    public int timeout;
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
                    updateChunkLoading(true);
                    StargateStructure.hideStructure(world, pos, facing);
                    this.notifyNearbyControllers();
                    if (SGCraftRebornConfig.LOG_STARGATE_EVENTS.get()) {
                        SGCraftReborn.LOGGER.info(String.format("STARGATE ADDED DIM: %d, X: %d Y: %d Z: %d", world.dimension.getType().getId(), pos.getX(), pos.getY(), pos.getZ()));
                    }
                } else {
                    updateChunkLoading(false);
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
        updateControllerBlockState();
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

    public ExtendedPos getExtendedPos() {
        return new ExtendedPos(getPos(), getWorld().getDimension().getType().getId());
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
            if (isMerged()) {
                updateChunkLoading(true);
            }
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

    private void updateControllerBlockState() {
        if (this.controllerPos == null || this.world == null || this.world.isRemote) return;

        TileEntity te = this.world.getTileEntity(this.controllerPos);

        if (te instanceof StargateControllerTileEntity) {
            ((StargateControllerTileEntity) te).onStargateStateChanged(this.vortexState);
        }
    }

    public void disconnect() {
        String targetAddress = this.getDialledAddress();
        this.setVortexState(StargateVortexState.CLOSING);
        this.setLocationPos(null);
        this.setDialledAddress("");
        this.setInitiator(false);
        this.timeout = 0;
        this.markDirty();

        if (targetAddress != null && !targetAddress.isEmpty()) {
            MinecraftServer server = world.getServer();
            if (server != null) {
                ExtendedPos remoteGatePos = StargateWorldData.findStargateUniversally(server, targetAddress);

                if (remoteGatePos != null) {
                    WorldServer targetWorld = server.getWorld(DimensionType.getById(remoteGatePos.getDimension()));

                    if (targetWorld != null) {
                        BlockPos remoteBlockPos = new BlockPos(remoteGatePos.getX(), remoteGatePos.getY(), remoteGatePos.getZ());
                        TileEntity remoteTE = targetWorld.getTileEntity(remoteBlockPos);

                        if (remoteTE instanceof StargateBaseTileEntity) {
                            StargateBaseTileEntity remoteGate = (StargateBaseTileEntity) remoteTE;
                            if (remoteGate.getVortexState() != StargateVortexState.CLOSING && remoteGate.getVortexState() != StargateVortexState.IDLE) {
                                remoteGate.disconnect();
                            }
                        }
                    }
                }
            }
        }
    }

    public void startDialing(String targetAddress, ExtendedPos targetLocation, boolean isInitiator) {
        setVortexState(StargateVortexState.DIALLING);
        setDialledAddress(targetAddress);
        setLocationPos(targetLocation);
        setInitiator(isInitiator);
        this.timeout = DIALLING_TIME;
        System.out.println("Dialing...");
        this.markDirty();
    }

    @Override
    public void tick() {
        if (world.isRemote) return;

        if (!isMerged()) {
            disconnect();
        }

        switch (this.vortexState) {
            case DIALLING:
                if (this.timeout > 0) {
                    this.timeout--;
                } else {
                    this.setVortexState(StargateVortexState.ACTIVE);
                    this.timeout = 1000;
                }
                break;

            case ACTIVE:
                if (this.timeout > 0) {
                    this.timeout--;
                    handleEntityTeleportation();
                } else {
                    disconnect();
                }
                break;

            case CLOSING:
                this.setVortexState(StargateVortexState.IDLE);
                break;

            case IDLE:
            default:
                break;
        }
    }

    private AxisAlignedBB getEventHorizonBoundingBox() {
        EnumFacing facing = world.getBlockState(pos).get(StargateBaseBlock.FACING);

        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        double minY = y + 1.0;
        double maxY = y + 4.0;

        if (facing.getAxis() == EnumFacing.Axis.Z) {
            double minX = x - 1.0;
            double maxX = x + 2.0;
            double minZ = z + 0.4;
            double maxZ = z + 0.6;

            return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        } else {
            double minX = x + 0.4;
            double maxX = x + 0.6;
            double minZ = z - 1.0;
            double maxZ = z + 2.0;

            return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    private void handleEntityTeleportation() {
        if (this.getConnectedLoc() == null) return;

        AxisAlignedBB eventHorizonBox = getEventHorizonBoundingBox();
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, eventHorizonBox);

        for (net.minecraft.entity.Entity entity : entities) {
            if (entity.timeUntilPortal > 0) continue;
            teleportEntity(entity);

            SGCraftReborn.LOGGER.log(Level.INFO, "Teleportation !");
        }
    }

    private void teleportEntity(net.minecraft.entity.Entity entity) {
        if (this.getConnectedLoc() == null || world.isRemote) return;

        MinecraftServer server = world.getServer();
        if (server == null) return;

        int targetDimId = connectedLoc.getDimension();
        BlockPos targetBasePos = new BlockPos(connectedLoc.getX(), connectedLoc.getY(), connectedLoc.getZ());
        WorldServer targetWorld = server.getWorld(Objects.requireNonNull(DimensionType.getById(targetDimId)));

        if (targetWorld == null) return;

        double tempDestX = targetBasePos.getX() + 0.5;
        double tempDestY = targetBasePos.getY() + 1.0;
        double tempDestZ = targetBasePos.getZ() + 0.5;

        float tempTargetPitch = entity.rotationPitch;
        float tempTargetYaw = entity.rotationYaw;

        IBlockState targetState = targetWorld.getBlockState(targetBasePos);
        if (targetState.getBlock() instanceof StargateBaseBlock) {
            EnumFacing targetFacing = targetState.get(StargateBaseBlock.FACING);

            tempDestX += targetFacing.getXOffset() * 1.5;
            tempDestZ += targetFacing.getZOffset() * 1.5;

            tempTargetYaw = targetFacing.getHorizontalAngle();
        }

        entity.timeUntilPortal = 40;
        boolean isCrossDimension = (world.getDimension().getType().getId() != targetDimId);

        final double destX = tempDestX;
        final double destY = tempDestY;
        final double destZ = tempDestZ;
        final float targetYaw = tempTargetYaw;
        final float targetPitch = tempTargetPitch;
        final DimensionType dimensionType = targetWorld.getDimension().getType();

        server.addScheduledTask(() -> {
            if (entity instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) entity;

                if (isCrossDimension) {
                    StargateTeleporter teleporter = new StargateTeleporter(destX, destY, destZ, targetYaw, targetPitch);
                    player.changeDimension(dimensionType, teleporter);
                } else {
                    player.connection.setPlayerLocation(destX, destY, destZ, targetYaw, targetPitch);
                }

            } else {
                if (isCrossDimension) {
                    StargateTeleporter teleporter = new StargateTeleporter(destX, destY, destZ, targetYaw, targetPitch);
                    entity.changeDimension(dimensionType, teleporter);
                } else {
                    entity.setLocationAndAngles(destX, destY, destZ, targetYaw, targetPitch);
                }
            }
        });
    }

    private void updateChunkLoading(boolean load) {
        if (world == null || world.isRemote || !(world instanceof WorldServer)) return;

        WorldServer serverWorld = (WorldServer) world;
        ChunkPos centerPos = new ChunkPos(pos);

        int range = SGCraftRebornConfig.CHUNK_LOADING_RANGE.get();

        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                serverWorld.func_212414_b(centerPos.x + x, centerPos.z + z, load);
            }
        }
    }

    @Override
    public void remove() {
        if (this.world != null && !this.world.isRemote) {
            if (isMerged()) {
                updateChunkLoading(false);
            }

            if (this.vortexState != StargateVortexState.IDLE) {
                disconnect();
            }

            if (this.controllerPos != null) {
                TileEntity te = world.getTileEntity(this.controllerPos);
                if (te instanceof StargateControllerTileEntity) {
                    ((StargateControllerTileEntity) te).unlink();
                }
            }
        }
        super.remove();
    }
}
