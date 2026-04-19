package fr.azures04.sgcraftreborn.common.registries.tiles;

import fr.azures04.sgcraftreborn.common.Constants;
import fr.azures04.sgcraftreborn.common.config.SGCraftRebornConfig;
import fr.azures04.sgcraftreborn.common.inventories.StargateBaseCamouflageContainer;
import fr.azures04.sgcraftreborn.common.registries.ModTilesEntities;
import fr.azures04.sgcraftreborn.common.registries.blocks.StargateBaseBlock;
import fr.azures04.sgcraftreborn.common.registries.structures.StargateStructure;
import fr.azures04.sgcraftreborn.common.registries.tiles.states.StargateIrisState;
import fr.azures04.sgcraftreborn.common.registries.tiles.states.StargateVortexState;
import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import fr.azures04.sgcraftreborn.common.world.StargateAddressing;
import fr.azures04.sgcraftreborn.common.world.StargateTeleporter;
import fr.azures04.sgcraftreborn.common.world.data.StargateWorldData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class StargateBaseTileEntity extends TileEntity implements ITickable, IInteractionObject {

    static final int DIALLING_TIME = 40;
    static final int INTER_DIALLING_TIME = 10;
    static final int TRANSIENT_DURATION = 20;

    private boolean isMerged, isInitiator, hasChevronUpgrade, hasIrisUpgrade, isClientInitialized;
    private ExtendedPos connectedLoc;
    private BlockPos controllerPos;
    private String dialledAddress = "", address = "";
    private double energyInBuffer, ringAngle, lastRingAngle;
    private int numEngagedChevrons, irisPhase, lastIrisPhase;

    private StargateIrisState irisState = StargateIrisState.OPEN;
    private StargateVortexState vortexState = StargateVortexState.IDLE;
    public int timeout, irisTimeout;

    private final ItemStackHandler inventory = new ItemStackHandler(5);
    private final LazyOptional<IItemHandler> inventoryHolder = LazyOptional.of(() -> inventory);

    public StargateBaseTileEntity() { super(ModTilesEntities.STARGATE_BASE_BLOCK); }
    public StargateBaseTileEntity(TileEntityType<?> tileEntityTypeIn) { super(tileEntityTypeIn); }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        compound.setBoolean("isMerged", isMerged);
        compound.setBoolean("isInitiator", isInitiator);
        compound.setString("dialledAddress", dialledAddress == null ? "" : dialledAddress);
        compound.setString("address", address == null ? "" : address);
        compound.setDouble("energyInBuffer", energyInBuffer);
        compound.setInt("numEngagedChevrons", numEngagedChevrons);
        compound.setBoolean("hasChevronUpgrade", hasChevronUpgrade);
        compound.setBoolean("hasIrisUpgrade", hasIrisUpgrade);
        compound.setInt("irisState", irisState != null ? irisState.ordinal() : 0);
        compound.setInt("irisTimeout", irisTimeout);
        compound.setInt("vortexState", vortexState != null ? vortexState.ordinal() : 0);
        compound.setInt("timeout", timeout);

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
        return super.write(compound);
    }

    @Override
    public void read(NBTTagCompound compound) {
        super.read(compound);
        isMerged = compound.getBoolean("isMerged");
        isInitiator = compound.getBoolean("isInitiator");
        dialledAddress = compound.getString("dialledAddress");
        address = compound.getString("address");
        energyInBuffer = compound.getDouble("energyInBuffer");
        numEngagedChevrons = compound.getInt("numEngagedChevrons");
        hasChevronUpgrade = compound.getBoolean("hasChevronUpgrade");
        hasIrisUpgrade = compound.getBoolean("hasIrisUpgrade");
        irisState = StargateIrisState.values()[compound.getInt("irisState")];
        irisTimeout = compound.getInt("irisTimeout");
        vortexState = StargateVortexState.values()[compound.getInt("vortexState")];
        timeout = compound.getInt("timeout");

        if (compound.contains("connectedX", 3)) {
            connectedLoc = new ExtendedPos(compound.getInt("connectedX"), compound.getInt("connectedY"),
                    compound.getInt("connectedZ"), compound.getInt("connectedD"));
        }
        if (compound.contains("controllerX", 3)) {
            controllerPos = new BlockPos(compound.getInt("controllerX"), compound.getInt("controllerY"), compound.getInt("controllerZ"));
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() { return new SPacketUpdateTileEntity(pos, 1, write(new NBTTagCompound())); }
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) { read(pkt.getNbtCompound()); }
    @Override
    public NBTTagCompound getUpdateTag() { return write(new NBTTagCompound()); }

    public void sync() {
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public void tick() {
        if (world.isRemote) {
            int targetPhase = (irisState == StargateIrisState.OPEN || irisState == StargateIrisState.OPENING) ? 60 : 0;
            if (!isClientInitialized) {
                irisPhase = targetPhase;
                lastIrisPhase = targetPhase;
                isClientInitialized = true;
            }

            lastRingAngle = ringAngle;
            lastIrisPhase = irisPhase;

            if (vortexState == StargateVortexState.DIALLING && timeout > 0 && dialledAddress != null) {
                timeout--;
                int cycleTime = DIALLING_TIME + INTER_DIALLING_TIME;
                int requiredChevrons = dialledAddress.length();
                int elapsedTime = (requiredChevrons * cycleTime + TRANSIENT_DURATION) - timeout;

                if (elapsedTime <= requiredChevrons * cycleTime && (elapsedTime % cycleTime) < DIALLING_TIME) {
                    ringAngle -= 5.0;
                    if (ringAngle < 0) ringAngle += 360.0;
                }
            }

            if (irisPhase < targetPhase) irisPhase++;
            if (irisPhase > targetPhase) irisPhase--;
            return;
        }

        if (!isMerged()) {
            if (vortexState != StargateVortexState.IDLE) disconnect();
            return;
        }

        if (irisTimeout > 0 && --irisTimeout <= 0) {
            irisState = (irisState == StargateIrisState.CLOSING) ? StargateIrisState.CLOSED : StargateIrisState.OPEN;
            sync();
        }

        switch (vortexState) {
            case DIALLING:
                if (this.timeout > 0 && this.dialledAddress != null) {
                    this.timeout--;

                    int cycleTime = DIALLING_TIME + INTER_DIALLING_TIME;
                    int requiredChevrons = this.dialledAddress.length();
                    int totalDialTime = requiredChevrons * cycleTime + TRANSIENT_DURATION;
                    int elapsedTime = totalDialTime - this.timeout;

                    if (elapsedTime <= requiredChevrons * cycleTime && (elapsedTime % cycleTime) == DIALLING_TIME) {
                        this.numEngagedChevrons++;
                        this.sync();
                    }

                    if (this.timeout < TRANSIENT_DURATION) {
                        if (this.irisState == StargateIrisState.OPEN || this.irisState == StargateIrisState.OPENING) {
                            handleKawooshVaporization();
                        }
                    }
                } else {
                    numEngagedChevrons = dialledAddress != null ? dialledAddress.length() : 0;
                    vortexState = StargateVortexState.ACTIVE;
                    int openTime = SGCraftRebornConfig.SECONDS_TO_STAY_OPEN.get();
                    timeout = openTime > 0 ? openTime * 20 : 999999;
                    updateControllerBlockState();
                    sync();
                }
                break;
            case ACTIVE:
                if (timeout > 0) {
                    timeout--;
                    if (irisState == StargateIrisState.OPEN) handleEntityTeleportation();
                    else if (irisState == StargateIrisState.CLOSED) handleEntityVaporization();
                } else disconnect();
                break;
            case CLOSING:
                vortexState = StargateVortexState.IDLE;
                updateControllerBlockState();
                sync();
                break;
            default: break;
        }
    }

    public void startDialing(String targetAddress, ExtendedPos targetLocation, boolean isInitiator) {
        if (targetAddress == null || targetAddress.isEmpty() || getAddress().startsWith(targetAddress)) {
            disconnect();
            return;
        }

        StargateBaseTileEntity remoteGate = null;
        int requiredChevrons = targetAddress.length();

        if (isInitiator) {
            remoteGate = getRemoteGate(targetAddress);
            if (remoteGate == null || remoteGate.getVortexState() != StargateVortexState.IDLE) {
                disconnect();
                return;
            }

            if (world.getDimension().getType().getId() == remoteGate.getWorld().getDimension().getType().getId() && requiredChevrons > 7) {
                requiredChevrons = 7;
            }

            int maxChevrons = hasChevronUpgrade ? 9 : 7;
            if (requiredChevrons > maxChevrons) requiredChevrons = maxChevrons;
            targetAddress = targetAddress.substring(0, requiredChevrons);
        }

        vortexState = StargateVortexState.DIALLING;
        dialledAddress = targetAddress;
        connectedLoc = targetLocation;
        this.isInitiator = isInitiator;
        numEngagedChevrons = 0;
        timeout = requiredChevrons * (DIALLING_TIME + INTER_DIALLING_TIME) + TRANSIENT_DURATION;

        updateControllerBlockState();
        sync();

        if (isInitiator && remoteGate != null) {
            String senderAddress = getAddress();
            if (senderAddress.length() > requiredChevrons) senderAddress = senderAddress.substring(0, requiredChevrons);
            remoteGate.startDialing(senderAddress, getExtendedPos(), false);
        }
    }

    public void disconnect() {
        String targetAddress = dialledAddress;
        vortexState = StargateVortexState.CLOSING;
        connectedLoc = null;
        dialledAddress = "";
        isInitiator = false;
        numEngagedChevrons = 0;
        timeout = 0;

        updateControllerBlockState();
        sync();

        if (targetAddress != null && !targetAddress.isEmpty()) {
            StargateBaseTileEntity remoteGate = getRemoteGate(targetAddress);
            if (remoteGate != null && remoteGate.getVortexState() != StargateVortexState.CLOSING && remoteGate.getVortexState() != StargateVortexState.IDLE) {
                remoteGate.disconnect();
            }
        }
    }

    public void setIrisDeployed(boolean deploy) {
        if (deploy && irisState == StargateIrisState.OPEN) {
            irisState = StargateIrisState.CLOSING;
            irisTimeout = 60;
            sync();
        } else if (!deploy && irisState == StargateIrisState.CLOSED) {
            irisState = StargateIrisState.OPENING;
            irisTimeout = 60;
            sync();
        }
    }

    public void setMerged(boolean merged) {
        if (isMerged != merged) {
            isMerged = merged;
            EnumFacing facing = getBlockState().get(StargateBaseBlock.FACING);

            if (!world.isRemote) {
                updateChunkLoading(merged);
                if (merged) {
                    StargateStructure.hideStructure(world, pos, facing);
                    notifyNearbyControllers();
                } else {
                    StargateStructure.showStructure(world, pos, facing);
                    if (controllerPos != null) {
                        TileEntity te = world.getTileEntity(controllerPos);
                        if (te instanceof StargateControllerTileEntity) ((StargateControllerTileEntity) te).unlink();
                        controllerPos = null;
                    }
                }
            }
            sync();
        }
    }

    private void handleEntityVaporization() {
        if (connectedLoc == null) return;
        AxisAlignedBB eventHorizonBox = getEventHorizonBoundingBox();

        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, eventHorizonBox)) {
            if (entity instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) entity;
                if (player.isCreative()) player.sendMessage(new TextComponentString("L'iris est fermé !"));
                if (!SGCraftRebornConfig.PRESERVE_INVENTORY.get()) player.inventory.clear();
            }
            entity.attackEntityFrom(DamageSource.FLY_INTO_WALL, Float.MAX_VALUE);
        }
    }

    private void handleEntityTeleportation() {
        if (connectedLoc == null) return;
        StargateBaseTileEntity remoteGate = getRemoteGate(dialledAddress);

        if (remoteGate == null || remoteGate.getIrisState() != StargateIrisState.OPEN) {
            handleEntityVaporization();
            return;
        }

        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, getEventHorizonBoundingBox())) {
            if (entity.timeUntilPortal <= 0) teleportEntity(entity);
        }
    }

    private void teleportEntity(Entity entity) {
        if (connectedLoc == null || world.isRemote) return;
        MinecraftServer server = world.getServer();
        if (server == null) return;

        WorldServer targetWorld = server.getWorld(Objects.requireNonNull(DimensionType.getById(connectedLoc.getDimension())));
        if (targetWorld == null) return;

        BlockPos targetBasePos = new BlockPos(connectedLoc.getX(), connectedLoc.getY(), connectedLoc.getZ());
        double destX = targetBasePos.getX() + 0.5, destY = targetBasePos.getY() + 1.0, destZ = targetBasePos.getZ() + 0.5;
        float targetYaw = entity.rotationYaw;

        IBlockState targetState = targetWorld.getBlockState(targetBasePos);
        if (targetState.getBlock() instanceof StargateBaseBlock) {
            EnumFacing targetFacing = targetState.get(StargateBaseBlock.FACING);
            destX += targetFacing.getXOffset() * 1.5;
            destZ += targetFacing.getZOffset() * 1.5;
            targetYaw = targetFacing.getHorizontalAngle();
        }

        entity.timeUntilPortal = 40;
        boolean isCrossDimension = (world.getDimension().getType().getId() != connectedLoc.getDimension());
        DimensionType dimType = targetWorld.getDimension().getType();

        final double fX = destX, fY = destY, fZ = destZ;
        final float fYaw = targetYaw, fPitch = entity.rotationPitch;

        server.addScheduledTask(() -> {
            if (entity instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) entity;
                if (isCrossDimension) player.changeDimension(dimType, new StargateTeleporter(fX, fY, fZ, fYaw, fPitch));
                else player.connection.setPlayerLocation(fX, fY, fZ, fYaw, fPitch);
            } else {
                if (isCrossDimension) entity.changeDimension(dimType, new StargateTeleporter(fX, fY, fZ, fYaw, fPitch));
                else entity.setLocationAndAngles(fX, fY, fZ, fYaw, fPitch);
            }
        });
    }

    private AxisAlignedBB getEventHorizonBoundingBox() {
        EnumFacing facing = world.getBlockState(pos).get(StargateBaseBlock.FACING);
        double x = pos.getX(), y = pos.getY(), z = pos.getZ();
        double minY = y + 1.0, maxY = y + 4.0;

        return facing.getAxis() == EnumFacing.Axis.Z
                ? new AxisAlignedBB(x - 1.0, minY, z + 0.4, x + 2.0, maxY, z + 0.6)
                : new AxisAlignedBB(x + 0.4, minY, z - 1.0, x + 0.6, maxY, z + 2.0);
    }

    public StargateBaseTileEntity getRemoteGate(String targetAddress) {
        if (targetAddress == null || targetAddress.isEmpty() || world.getServer() == null) return null;
        ExtendedPos remotePos = StargateWorldData.findStargateUniversally(world.getServer(), targetAddress);

        if (remotePos != null) {
            WorldServer targetWorld = world.getServer().getWorld(DimensionType.getById(remotePos.getDimension()));
            if (targetWorld != null) {
                TileEntity te = targetWorld.getTileEntity(new BlockPos(remotePos.getX(), remotePos.getY(), remotePos.getZ()));
                if (te instanceof StargateBaseTileEntity) return (StargateBaseTileEntity) te;
            }
        }
        return null;
    }

    private void updateControllerBlockState() {
        if (controllerPos == null || world == null || world.isRemote) return;
        TileEntity te = world.getTileEntity(controllerPos);
        if (te instanceof StargateControllerTileEntity) ((StargateControllerTileEntity) te).onStargateStateChanged(vortexState);
    }

    private void notifyNearbyControllers() {
        double rX = SGCraftRebornConfig.LINK_RANGE_X.get() * 2.0;
        double rY = SGCraftRebornConfig.LINK_RANGE_Y.get() * 2.0;
        double rZ = SGCraftRebornConfig.LINK_RANGE_Z.get() * 2.0;

        for (TileEntity te : world.loadedTileEntityList) {
            if (te instanceof StargateControllerTileEntity && new AxisAlignedBB(pos).grow(rX, rY, rZ).contains(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ())) {
                ((StargateControllerTileEntity) te).getLinkedStargateTE(world);
            }
        }
    }

    private void updateChunkLoading(boolean load) {
        if (world == null || world.isRemote || !(world instanceof WorldServer)) return;
        ChunkPos centerPos = new ChunkPos(pos);
        int range = SGCraftRebornConfig.CHUNK_LOADING_RANGE.get();

        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                world.func_212414_b(centerPos.x + x, centerPos.z + z, load);
            }
        }
    }

    @Override
    public void onLoad() {
        if (!world.isRemote) {
            StargateWorldData.get(world).register(getAddress(), new ExtendedPos(pos, world.getDimension().getType().getId()));
            if (isMerged()) updateChunkLoading(true);
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (!world.isRemote) StargateWorldData.get(world).unregister(getAddress());
    }

    @Override
    public void remove() {
        if (world != null && !world.isRemote) {
            if (isMerged()) updateChunkLoading(false);
            if (vortexState != StargateVortexState.IDLE) disconnect();
            if (controllerPos != null) {
                TileEntity te = world.getTileEntity(controllerPos);
                if (te instanceof StargateControllerTileEntity) ((StargateControllerTileEntity) te).unlink();
            }
        }
        super.remove();
    }

    public String getAddress() {
        if (address == null || address.trim().isEmpty()) {
            address = StargateAddressing.generateAddress();
            markDirty();
        }
        return address;
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
    public boolean hasChevronUpgrade() {
        return hasChevronUpgrade;
    }
    public boolean hasIrisUpgrade() {
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
    public ExtendedPos getExtendedPos() {
        return new ExtendedPos(getPos(), getWorld().getDimension().getType().getId());
    }
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos; markDirty();
    }

    public void setInitiator(boolean initiator) {
        isInitiator = initiator; markDirty();
    }

    public void setLocationPos(ExtendedPos location) {
        connectedLoc = location; markDirty();
    }

    public void setDialledAddress(String dialledAddress) {
        this.dialledAddress = dialledAddress; markDirty();
    }

    public void setEnergyInBuffer(double energyInBuffer) {
        this.energyInBuffer = energyInBuffer; markDirty();
    }

    public void setNumEngagedChevrons(int numEngagedChevrons) {
        this.numEngagedChevrons = numEngagedChevrons; markDirty();
    }


    public void setHasChevronUpgrade(boolean hasChevronUpgrade) {
        this.hasChevronUpgrade = hasChevronUpgrade; sync();
    }

    public void setHasIrisUpgrade(boolean hasIrisUpgrade) {
        this.hasIrisUpgrade = hasIrisUpgrade; sync();
    }

    public void setIrisState(StargateIrisState irisState) {
        this.irisState = irisState; markDirty();
    }

    public void setIrisPhase(int irisPhase) {
        this.irisPhase = irisPhase; markDirty();
    }

    public void setLastIrisPhase(int lastIrisPhase) {
        this.lastIrisPhase = lastIrisPhase; markDirty();
    }

    public void setRingAngle(double ringAngle) {
        this.ringAngle = ringAngle; markDirty();
    }

    public void setLastRingAngle(double lastRingAngle) {
        this.lastRingAngle = lastRingAngle; markDirty();
    }


    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new StargateBaseCamouflageContainer(playerInventory, pos);
    }

    @Override
    public String getGuiID() {
        return Constants.MOD_ID + ":base_camouflage";
    }

    @Override
    public ITextComponent getName() {
        return new TextComponentTranslation("container.sgcraftreborn.base");
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Nullable @Override public ITextComponent getCustomName() { return null; }

    @Nonnull @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return inventoryHolder.cast();
        return super.getCapability(cap, side);
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        inventoryHolder.invalidate();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos).grow(3.0, 5.0, 3.0);
    }

    private void handleKawooshVaporization() {
        AxisAlignedBB box = getKawooshBoundingBox();
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, box);

        for (Entity entity : entities) {
            entity.attackEntityFrom(DamageSource.MAGIC, Float.MAX_VALUE);
        }
    }

    private AxisAlignedBB getKawooshBoundingBox() {
        EnumFacing facing = world.getBlockState(pos).get(StargateBaseBlock.FACING);

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 2.5;
        double z = pos.getZ() + 0.5;
        double r = 2.0;
        double depth = 4.5;

        double minX = x - r, maxX = x + r;
        double minY = y - r, maxY = y + r;
        double minZ = z - r, maxZ = z + r;

        if (facing.getAxis() == EnumFacing.Axis.Z) {
            minZ = z - 0.5; maxZ = z + 0.5;
        } else if (facing.getAxis() == EnumFacing.Axis.X) {
            minX = x - 0.5; maxX = x + 0.5;
        } else {
            minY = y - 0.5; maxY = y + 0.5;
        }

        AxisAlignedBB box = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        return box.expand(facing.getXOffset() * depth, facing.getYOffset() * depth, facing.getZOffset() * depth);
    }
}